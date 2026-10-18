package org.docshare.yangmvc.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.yangmvc.mvc.except.MVCException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 控制器单例模式（ThreadLocal 请求上下文）的回归测试。
 *
 * <p>覆盖评审里 S1（字段审计）、S2（首次并发只产生一个实例）、S5（setSingle(false) 立即生效）
 * 以及"在 action 里才调 setSingle(true)"的兜底路径。
 */
public class SingletonModeTest {

	/** 无实例字段：可以安全做单例 */
	public static class StatelessCtl extends Controller {
		static final AtomicInteger created = new AtomicInteger();
		static final Set<Integer> served = ConcurrentHashMap.newKeySet();

		public StatelessCtl() {
			created.incrementAndGet();
			setSingle(true);
		}

		public String hi() {
			served.add(System.identityHashCode(this));
			return "ok";
		}
	}

	/** 有实例字段：单例下会跨请求共享，审计应该报出来 */
	public static class WithFieldCtl extends Controller {
		private int counter;

		public WithFieldCtl() {
			setSingle(true);
		}

		public String hi() {
			counter++;
			return "ok" + counter;
		}
	}

	/** 普通控制器：每次请求一个实例 */
	public static class NotSingleCtl extends Controller {
		public String hi() {
			return "ok";
		}
	}

	/** 在 action 里才开启单例（不推荐，但要有兜底） */
	public static class LateCtl extends Controller {
		static final AtomicInteger created = new AtomicInteger();

		public LateCtl() {
			created.incrementAndGet();
		}

		public String hi() {
			setSingle(true);
			return "ok";
		}
	}

	private boolean oldReloadable;
	private boolean oldStrict;
	private Loader oldLoader;

	@Before
	public void setUp() {
		oldReloadable = Config.reloadable;
		oldStrict = Config.strict_singleton;
		oldLoader = MVCFilter.loader;

		Config.reloadable = false; // 走非热加载路径，避免测试里启动 watcher
		Config.strict_singleton = false;
		MVCFilter.loader = new Loader();
		Loader.clearSingletons();
		CallCacheMap.clear();
	}

	@After
	public void tearDown() {
		Config.reloadable = oldReloadable;
		Config.strict_singleton = oldStrict;
		MVCFilter.loader = oldLoader;
		Loader.clearSingletons();
		CallCacheMap.clear();
		Controller.clearCurrentContext();
	}

	@Test
	public void fieldAuditThrowsInStrictMode() {
		Config.strict_singleton = true;
		try {
			new WithFieldCtl();
			fail("有实例字段的单例控制器在 strict 模式下应该抛异常");
		} catch (MVCException e) {
			assertTrue("异常信息里应该指出是哪个字段: " + e.getMessage(), e.getMessage().contains("counter"));
		}
	}

	@Test
	public void fieldAuditOnlyWarnsByDefault() {
		Config.strict_singleton = false;
		new WithFieldCtl(); // 不抛异常
	}

	@Test
	public void statelessSingletonPassesStrictAudit() {
		Config.strict_singleton = true;
		new StatelessCtl(); // 只有 static 字段，不应该抛
	}

	@Test
	public void concurrentFirstCallServesWithOneInstance() throws Exception {
		StatelessCtl.created.set(0);
		StatelessCtl.served.clear();

		final String cname = StatelessCtl.class.getName();
		final int threads = 32;
		final CountDownLatch start = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(threads);
		final AtomicReference<Throwable> error = new AtomicReference<Throwable>();

		for (int i = 0; i < threads; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						start.await();
						invoke(cname, "hi");
					} catch (Throwable t) {
						error.compareAndSet(null, t);
					} finally {
						done.countDown();
					}
				}
			}, "singleton-" + i).start();
		}
		start.countDown();
		assertTrue("并发调用超时", done.await(30, TimeUnit.SECONDS));
		assertNull(String.valueOf(error.get()), error.get());

		assertEquals("所有请求应该由同一个实例服务", 1, StatelessCtl.served.size());
		assertEquals("单例表里应该只有一条", 1, Loader.singleMap.size());
	}

	@Test
	public void nonSingleControllerIsNotCached() throws Exception {
		invoke(NotSingleCtl.class.getName(), "hi");
		invoke(NotSingleCtl.class.getName(), "hi");
		assertTrue("非单例控制器不应该进单例表", Loader.singleMap.isEmpty());
	}

	@Test
	public void lateSetSingleStillGetsCached() throws Exception {
		LateCtl.created.set(0);
		invoke(LateCtl.class.getName(), "hi");
		invoke(LateCtl.class.getName(), "hi");
		assertEquals("第二次请求应该复用第一次创建的实例", 1, LateCtl.created.get());
		assertEquals(1, Loader.singleMap.size());
	}

	@Test
	public void setSingleFalseRemovesFromMap() {
		StatelessCtl c = new StatelessCtl();
		Loader.singleMap.put(c.getClass().getName(), c);
		assertTrue(Loader.singleMap.containsKey(c.getClass().getName()));

		c.setSingle(false);
		assertFalse("取消单例后应该立即从单例表移除", Loader.singleMap.containsKey(c.getClass().getName()));
	}

	// ------------------------------------------------------------------

	private void invoke(String cname, String method) throws Exception {
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
		Mockito.when(resp.getContentType()).thenReturn(null);
		Mockito.when(req.getContentType()).thenReturn(null);

		boolean handled = MVCFilter.loader.call("/t/" + method, cname, method, req, resp);
		assertTrue("Loader.call 应该处理这个请求", handled);
		Controller.clearCurrentContext();
	}
}
