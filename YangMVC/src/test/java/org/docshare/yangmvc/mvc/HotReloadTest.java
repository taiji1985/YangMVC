package org.docshare.yangmvc.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 热加载（ClassLoader 换代）的回归测试。
 *
 * <p>覆盖：
 * <ul>
 *   <li>改 class 之后能拿到新版本，且不同代是两个不同的 ClassLoader；</li>
 *   <li>service/pojo 默认排除；</li>
 *   <li>并发加载不会卡死（历史 bug：非 parallel-capable + 在加载锁里调 ReflectASM）；</li>
 *   <li>换代后旧 loader 可以被回收（否则 Metaspace 只涨不落）。</li>
 * </ul>
 */
public class HotReloadTest {

	private File root;
	private boolean oldReloadable;
	private String oldRoot;
	private String oldController;
	private int oldInterval;
	private int oldDebounce;
	private String oldExclude;
	private boolean oldReflectasm;

	@Before
	public void setUp() throws Exception {
		oldReloadable = Config.reloadable;
		oldRoot = Config.class_root;
		oldController = Config.controller;
		oldInterval = Config.reload_scan_interval_ms;
		oldDebounce = Config.reload_debounce_ms;
		oldExclude = Config.reload_exclude_packages;
		oldReflectasm = Config.reload_reflectasm;

		root = new File(System.getProperty("java.io.tmpdir"), "yangmvc-hot-" + System.nanoTime());
		root.mkdirs();

		Config.reloadable = true;
		Config.class_root = root.getAbsolutePath();
		Config.controller = "hot";
		Config.reload_scan_interval_ms = 0; // 测试里手动触发，不起后台线程
		Config.reload_debounce_ms = 0;
		Config.reload_exclude_packages = "*.service,*.pojo";
	}

	@After
	public void tearDown() {
		Config.reloadable = oldReloadable;
		Config.class_root = oldRoot;
		Config.controller = oldController;
		Config.reload_scan_interval_ms = oldInterval;
		Config.reload_debounce_ms = oldDebounce;
		Config.reload_exclude_packages = oldExclude;
		Config.reload_reflectasm = oldReflectasm;
		MethodAccessCacher.clear();
		CallCacheMap.clear();
		Loader.clearSingletons();
	}

	// ------------------------------------------------------------------

	@Test
	public void reloadPicksUpNewBytecode() throws Exception {
		compile("hot", "Foo", "public String value(){ return \"v1\"; }");

		Reloader reloader = new Reloader(root.getAbsolutePath(), "hot");
		Class<?> v1 = reloader.load("hot.Foo");
		assertEquals("v1", invokeValue(v1));
		int gen1 = reloader.getGeneration();

		touchCompile("hot", "Foo", "public String value(){ return \"v2\"; }");

		Class<?> v2 = reloader.load("hot.Foo");
		assertEquals("v2", invokeValue(v2));
		assertTrue("换代后应该是不同的 ClassLoader", v1.getClassLoader() != v2.getClassLoader());
		assertTrue("代应该增加", reloader.getGeneration() > gen1);
	}

	@Test
	public void loaderAloneReadsLatestBytes() throws Exception {
		compile("hot", "Bar", "public String value(){ return \"a\"; }");
		Class<?> a = new YangClassLoader(1, root.getAbsolutePath(), "hot").loadClass("hot.Bar");
		assertEquals("a", invokeValue(a));

		touchCompile("hot", "Bar", "public String value(){ return \"b\"; }");
		Class<?> b = new YangClassLoader(2, root.getAbsolutePath(), "hot").loadClass("hot.Bar");
		assertEquals("b", invokeValue(b));
	}

	@Test
	public void excludedPackagesAreNotHotLoaded() {
		assertTrue(ReloadFilter.isReloadable("hot.Foo", "hot"));
		assertTrue(ReloadFilter.isReloadable("hot.api.UsrController", "hot"));
		assertFalse("service 默认排除", ReloadFilter.isReloadable("hot.service.VipService", "hot"));
		assertFalse("pojo 默认排除", ReloadFilter.isReloadable("hot.pojo.Paper", "hot"));
		assertFalse("包外不热加载", ReloadFilter.isReloadable("other.Foo", "hot"));

		// 也支持完整包前缀
		Config.reload_exclude_packages = "org.docshare.l2exam.oss";
		assertFalse(ReloadFilter.isReloadable("org.docshare.l2exam.oss.QiNiuCloud", "org.docshare.l2exam"));
		assertTrue(ReloadFilter.isReloadable("org.docshare.l2exam.api.UsrController", "org.docshare.l2exam"));
	}

	@Test
	public void scanDetectsChangeAndBumpsGeneration() throws Exception {
		compile("hot", "ScanFoo", "public String value(){ return \"s1\"; }");
		Reloader reloader = new Reloader(root.getAbsolutePath(), "hot");

		Class<?> v1 = reloader.load("hot.ScanFoo");
		assertEquals("s1", invokeValue(v1));
		int gen = reloader.getGeneration();
		reloader.scanOnce(); // 建立基线

		touchCompile("hot", "ScanFoo", "public String value(){ return \"s2\"; }");
		reloader.scanOnce(); // 发现变化 + debounce=0 → 换代

		assertTrue("scanOnce 应该换代", reloader.getGeneration() > gen);
		assertEquals("s2", invokeValue(reloader.load("hot.ScanFoo")));
	}

	/**
	 * 历史 bug：非 parallel-capable + 在加载锁里调 ReflectASM，会锁顺序反转。
	 * 这里并发加载大量类，跑不出来死锁即可。
	 */
	@Test(timeout = 120000)
	public void concurrentLoadingDoesNotDeadlock() throws Exception {
		final int classes = 40;
		for (int i = 0; i < classes; i++) {
			compile("hot.gen", "C" + i, "public int value(){ return " + i + "; }");
		}

		for (int round = 0; round < 10; round++) {
			final YangClassLoader loader = new YangClassLoader(round + 1, root.getAbsolutePath(), "hot");
			final int threads = 16;
			final CountDownLatch start = new CountDownLatch(1);
			final CountDownLatch done = new CountDownLatch(threads);
			final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
			for (int t = 0; t < threads; t++) {
				final int offset = t;
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							start.await();
							for (int i = 0; i < classes; i++) {
								int idx = (i + offset) % classes;
								Class<?> c = loader.loadClass("hot.gen.C" + idx);
								assertEquals(idx, c.getMethod("value").invoke(c.newInstance()));
							}
						} catch (Throwable e) {
							error.compareAndSet(null, e);
						} finally {
							done.countDown();
						}
					}
				}, "load-" + t).start();
			}
			start.countDown();
			assertTrue("并发加载超时（疑似死锁）", done.await(30, TimeUnit.SECONDS));
			assertNull(String.valueOf(error.get()), error.get());
		}
	}

	/**
	 * 换代后旧 loader 必须能被回收，否则 Metaspace 只涨不落（表现为越跑越卡）。
	 */
	@Test
	public void oldGenerationIsCollectable() throws Exception {
		compile("hot", "GcFoo", "public String value(){ return \"g\"; }");
		Reloader reloader = new Reloader(root.getAbsolutePath(), "hot");
		Class<?> c = reloader.load("hot.GcFoo");
		WeakReference<ClassLoader> ref = new WeakReference<ClassLoader>(c.getClassLoader());
		c = null;

		reloader.newGeneration();

		for (int i = 0; i < 50 && ref.get() != null; i++) {
			System.gc();
			Thread.sleep(50);
		}
		assertNull("旧代 ClassLoader 没有被回收（缓存里还钉着旧 Class）", ref.get());
	}

	@Test
	public void hotControllerActionIsDispatchable() throws Exception {
		String body = "import org.docshare.yangmvc.mvc.Controller;\n"
				+ "public class HotCtl extends Controller { public String hi(){ return \"hi-v1\"; } }";
		compileExtends("hot", "HotCtl", body, true);

		Reloader reloader = new Reloader(root.getAbsolutePath(), "hot");
		Class<?> c1 = reloader.load("hot.HotCtl");
		assertTrue(Controller.class.isAssignableFrom(c1));
		assertEquals("hi-v1", dispatch(c1, "hi"));

		String body2 = "import org.docshare.yangmvc.mvc.Controller;\n"
				+ "public class HotCtl extends Controller { public String hi(){ return \"hi-v2\"; } }";
		compileExtends("hot", "HotCtl", body2, true);
		new File(root, "hot/HotCtl.class").setLastModified(System.currentTimeMillis() + 5000);

		Class<?> c2 = reloader.load("hot.HotCtl");
		assertTrue("应该换了一代", c2.getClassLoader() != c1.getClassLoader());
		assertEquals("hi-v2", dispatch(c2, "hi"));
	}

	/** 模拟 Loader.call 的动作分发（findActionMethod + 反射调用） */
	private String dispatch(Class<?> cz, String methodName) throws Exception {
		Method m = Loader.findActionMethod(cz, methodName);
		assertNotNull("应该能找到 action: " + methodName, m);
		Controller ins = (Controller) cz.newInstance();
		Object ret = new Loader().runMethod(ins, MethodAccessCacher.getMethodAccess(cz.getName()), m, null, cz.getName());
		return String.valueOf(ret);
	}

	// ------------------------------------------------------------------

	private void compileExtends(String pkg, String cls, String body, boolean withClasspath) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertNotNull("需要 JDK（含 javac）才能运行热加载测试", compiler);
		File dir = new File(root, pkg.replace('.', '/'));
		assertTrue(dir.isDirectory() || dir.mkdirs());
		File src = new File(dir, cls + ".java");
		FileWriter w = new FileWriter(src);
		try {
			w.write("package " + pkg + ";\n");
			w.write(body + "\n");
		} finally {
			w.close();
		}
		List<String> args = new ArrayList<String>();
		if (withClasspath) {
			File classes = new File(Controller.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			args.add("-classpath");
			args.add(classes.getAbsolutePath());
		}
		args.add("-d");
		args.add(root.getAbsolutePath());
		args.add(src.getAbsolutePath());
		int rc = compiler.run(null, null, null, args.toArray(new String[0]));
		assertEquals("compile failed: " + src, 0, rc);
	}

	private void touchCompile(String pkg, String cls, String body) throws Exception {
		compile(pkg, cls, body);
		// 保证 mtime 一定变大（同一毫秒内编译两次时 Windows 上可能相等）
		File cf = new File(root, pkg.replace('.', '/') + "/" + cls + ".class");
		assertNotNull(cf);
		cf.setLastModified(System.currentTimeMillis() + 5000);
	}

	private void compile(String pkg, String cls, String body) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertNotNull("需要 JDK（含 javac）才能运行热加载测试", compiler);
		File dir = new File(root, pkg.replace('.', '/'));
		assertTrue(dir.isDirectory() || dir.mkdirs());
		File src = new File(dir, cls + ".java");
		FileWriter w = new FileWriter(src);
		try {
			w.write("package " + pkg + ";\n");
			w.write("public class " + cls + " {\n");
			w.write(body + "\n");
			w.write("}\n");
		} finally {
			w.close();
		}
		List<String> args = new ArrayList<String>();
		args.add("-d");
		args.add(root.getAbsolutePath());
		args.add(src.getAbsolutePath());
		int rc = compiler.run(null, null, null, args.toArray(new String[0]));
		assertEquals("compile failed: " + src, 0, rc);
	}

	private String invokeValue(Class<?> c) throws Exception {
		Object o = c.newInstance();
		Method m = c.getMethod("value");
		return String.valueOf(m.invoke(o));
	}
}
