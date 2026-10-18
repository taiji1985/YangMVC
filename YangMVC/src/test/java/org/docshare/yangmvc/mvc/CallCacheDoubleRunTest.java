package org.docshare.yangmvc.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * P0-1：缓存路径里方法抛异常时，同一次请求<b>不能</b>再执行一遍。
 *
 * <p>旧实现把异常吞掉后 {@code return false}，MVCFilter 会认为"没处理"，
 * 于是走正常路径又执行一次 → 重复插入/重复发码。
 */
public class CallCacheDoubleRunTest {

	public static class BoomController extends Controller {
		public static int calls = 0;

		public String boom() {
			calls++;
			throw new IllegalStateException("boom-from-test");
		}
	}

	private Loader oldLoader;

	@Before
	public void setUp() {
		// 真实运行时 loader 由 MVCFilter.init() 赋值；单测里手动给一个
		oldLoader = MVCFilter.loader;
		if (MVCFilter.loader == null) {
			MVCFilter.loader = new Loader();
		}
	}

	@After
	public void tearDown() {
		MVCFilter.loader = oldLoader;
		Controller.clearCurrentContext();
	}

	@Test
	public void exceptionInCachedPathDoesNotRunTwice() throws Exception {
		String uri = "/test/boom-" + System.nanoTime();
		BoomController.calls = 0;

		Class<?> cz = BoomController.class;
		MethodAccessCacher.put(cz.getName(), cz);
		Method m = MethodAccessCacher.getMethod(cz.getName(), "boom");
		assertNotNull(m);
		MethodAccess access = MethodAccessCacher.getMethodAccess(cz.getName());
		CallCacheMap.addCache(uri, cz, access, "boom", null, m);

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
		StringWriter sw = new StringWriter();
		Mockito.when(resp.getWriter()).thenReturn(new PrintWriter(sw));

		boolean handled = CallCacheMap.runCallCache(uri, req, resp);

		assertTrue("缓存路径的异常必须由它自己接管", handled);
		assertEquals("同一次请求只能执行一次", 1, BoomController.calls);
		Mockito.verify(resp).setStatus(500);
		assertTrue("错误信息应写回响应", sw.toString().contains("boom-from-test"));
	}
}
