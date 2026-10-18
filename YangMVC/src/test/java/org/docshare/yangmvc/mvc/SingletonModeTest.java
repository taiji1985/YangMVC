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

import org.docshare.yangmvc.mvc.anno.Singleton;
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

	/** 不调 setSingle，靠全局开关生效 */
	public static class GlobalCtl extends Controller {
		static final AtomicInteger created = new AtomicInteger();
		static final Set<Integer> served = ConcurrentHashMap.newKeySet();

		public GlobalCtl() {
			created.incrementAndGet();
		}

		public String hi() {
			served.add(System.identityHashCode(this));
			return "ok";
		}
	}

	/** 全局单例模式下显式退出 */
	public static class GlobalOptOutCtl extends Controller {
		static final AtomicInteger created = new AtomicInteger();

		public GlobalOptOutCtl() {
			created.incrementAndGet();
			setSingle(false);
		}

		public String hi() {
			return "ok";
		}
	}

	/** 全局单例 + 有字段：审计要在 Loader 侧触发 */
	public static class GlobalWithFieldCtl extends Controller {
		private int counter;

		public String hi() {
			counter++;
			return "ok";
		}
	}

	/** 用注解声明单例 */
	@Singleton
	public static class AnnotatedCtl extends Controller {
		static final AtomicInteger created = new AtomicInteger();
		static final Set<Integer> served = ConcurrentHashMap.newKeySet();

		public AnnotatedCtl() {
			created.incrementAndGet();
		}

		public String hi() {
			served.add(System.identityHashCode(this));
			return "ok";
		}
	}

	/** 注解了但显式退出单例 */
	@Singleton
	public static class AnnotatedOptOutCtl extends Controller {
		static final AtomicInteger created = new AtomicInteger();

		public AnnotatedOptOutCtl() {
			created.incrementAndGet();
			setSingle(false);
		}

		public String hi() {
			return "ok";
		}
	}

	/** 注解 + 有字段：审计要能报出来 */
	@Singleton
	public static class AnnotatedWithFieldCtl extends Controller {
		private int counter;

		public String hi() {
			counter++;
			return "ok";
		}
	}

	/** 注解标在基类上（@Inherited），子类也应该按单例 */
	@Singleton
	public static class AnnotatedBase extends Controller {
	}

	public static class InheritedCtl extends AnnotatedBase {
		static final AtomicInteger created = new AtomicInteger();
		static final Set<Integer> served = ConcurrentHashMap.newKeySet();

		public InheritedCtl() {
			created.incrementAndGet();
		}

		public String hi() {
			served.add(System.identityHashCode(this));
			return "ok";
		}
	}

	private boolean oldReloadable;
	private boolean oldStrict;
	private boolean oldGlobalSingle;
	private Loader oldLoader;

	@Before
	public void setUp() {
		oldReloadable = Config.reloadable;
		oldStrict = Config.strict_singleton;
		oldGlobalSingle = Config.single_all_controllers;
		oldLoader = MVCFilter.loader;

		Config.reloadable = false; // 走非热加载路径，避免测试里启动 watcher
		Config.strict_singleton = false;
		Config.single_all_controllers = false;
		MVCFilter.loader = new Loader();
		Loader.clearSingletons();
		CallCacheMap.clear();
	}

	@After
	public void tearDown() {
		Config.reloadable = oldReloadable;
		Config.strict_singleton = oldStrict;
		Config.single_all_controllers = oldGlobalSingle;
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

	// ---------------- 全局单例配置 ----------------

	@Test
	public void globalSwitchMakesControllersSingleton() throws Exception {
		Config.single_all_controllers = true;
		GlobalCtl.created.set(0);
		GlobalCtl.served.clear();

		invoke(GlobalCtl.class.getName(), "hi");
		invoke(GlobalCtl.class.getName(), "hi");

		assertEquals("全局开关下应该只创建一个实例", 1, GlobalCtl.created.get());
		assertEquals("两次请求应该由同一个实例服务", 1, GlobalCtl.served.size());
		assertEquals(1, Loader.singleMap.size());
	}

	@Test
	public void explicitFalseOptsOutOfGlobal() throws Exception {
		Config.single_all_controllers = true;
		GlobalOptOutCtl.created.set(0);

		invoke(GlobalOptOutCtl.class.getName(), "hi");
		invoke(GlobalOptOutCtl.class.getName(), "hi");

		assertEquals("setSingle(false) 应该能退出全局单例", 2, GlobalOptOutCtl.created.get());
		assertTrue(Loader.singleMap.isEmpty());
	}

	@Test
	public void globalSwitchAuditsFieldsInLoader() {
		Config.single_all_controllers = true;
		Config.strict_singleton = true;
		try {
			invoke(GlobalWithFieldCtl.class.getName(), "hi");
			fail("全局单例 + strict 模式下，有实例字段的控制器应该在 Loader 里被拦下");
		} catch (Exception e) {
			assertTrue("异常链里应该指出字段 counter: " + e, hasMessage(e, "counter"));
		}
	}

	// ---------------- @Singleton 注解 ----------------

	@Test
	public void annotatedControllerIsSingleton() throws Exception {
		AnnotatedCtl.created.set(0);
		AnnotatedCtl.served.clear();

		invoke(AnnotatedCtl.class.getName(), "hi");
		invoke(AnnotatedCtl.class.getName(), "hi");

		assertEquals(1, AnnotatedCtl.created.get());
		assertEquals("两次请求应该由同一个实例服务", 1, AnnotatedCtl.served.size());
		assertEquals(1, Loader.singleMap.size());
	}

	@Test
	public void annotatedControllerCanOptOut() throws Exception {
		AnnotatedOptOutCtl.created.set(0);

		invoke(AnnotatedOptOutCtl.class.getName(), "hi");
		invoke(AnnotatedOptOutCtl.class.getName(), "hi");

		assertEquals("setSingle(false) 应该能盖过注解", 2, AnnotatedOptOutCtl.created.get());
		assertTrue(Loader.singleMap.isEmpty());
	}

	@Test
	public void annotatedControllerIsAudited() {
		Config.strict_singleton = true;
		try {
			invoke(AnnotatedWithFieldCtl.class.getName(), "hi");
			fail("注解单例 + strict 模式下，有实例字段的控制器应该被拦下");
		} catch (Exception e) {
			assertTrue("异常链里应该指出字段 counter: " + e, hasMessage(e, "counter"));
		}
	}

	@Test
	public void annotationIsInherited() throws Exception {
		InheritedCtl.created.set(0);
		InheritedCtl.served.clear();

		invoke(InheritedCtl.class.getName(), "hi");
		invoke(InheritedCtl.class.getName(), "hi");

		assertEquals("基类上的 @Singleton 应该对子类生效", 1, InheritedCtl.created.get());
		assertEquals(1, InheritedCtl.served.size());
	}

	private static boolean hasMessage(Throwable e, String kw) {
		Throwable t = e;
		while (t != null) {
			if (t.getMessage() != null && t.getMessage().contains(kw)) {
				return true;
			}
			t = t.getCause();
		}
		return false;
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
