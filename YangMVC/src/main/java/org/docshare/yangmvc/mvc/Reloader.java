package org.docshare.yangmvc.mvc;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 热加载：把 {@link Config#controller} 包下的 class 交给一组"代"式类加载器。
 *
 * <p>工作方式（相对老实现的三处关键变化）：
 * <ul>
 *   <li><b>一个代一个 loader</b>：任一类变了就整代替换，不再"改一个类 new 一个 loader"，
 *       避免同一个类出现两个 Class 对象、以及"新旧混用"。</li>
 *   <li><b>后台扫描 + 防抖</b>：默认每 1 秒扫一次 class 目录，改动后等
 *       {@link Config#reload_debounce_ms} 才换代；请求路径上不再做文件 stat。</li>
 *   <li><b>换代时清缓存</b>：{@link MethodAccessCacher} / {@link CallCacheMap} / 单例表，
 *       否则旧 loader 永远回收不掉（Metaspace 只涨不落，表现为越跑越卡）。</li>
 * </ul>
 *
 * <p>{@code service/pojo} 等包由 {@link ReloadFilter} 排除，永远由父加载器加载（改了要重启），
 * 这样不会出现"父子各一份"。
 */
public class Reloader {
	private static final Logger logger = LoggerFactory.getLogger(Reloader.class);

	/** class 文件目录（解析后的） */
	protected String root;
	/** 类名 → 上次看到的 mtime */
	protected Map<String, Long> last_tm = new ConcurrentHashMap<String, Long>();
	/** 需要监测重新加载的包 */
	protected String reloadPkg;

	/** 兼容 GroovyReloader：当前 loader 的弱别名 */
	protected ClassLoader loader = null;
	/** 兼容 GroovyReloader：代数 */
	int loaderVersion = 0;

	/** 构造时传入的根目录（未解析） */
	private final String declaredRoot;
	private volatile YangClassLoader current;
	private final AtomicInteger generation = new AtomicInteger();
	private volatile long pendingChangeAt = 0;
	private volatile boolean dirty = false;
	private volatile boolean watcherStarted = false;
	private volatile boolean rootResolved = false;

	public Reloader(String clsRoot, String reloadPkg) {
		this.declaredRoot = clsRoot;
		this.reloadPkg = reloadPkg;
	}

	Reloader() {
		this.declaredRoot = null;
	}

	public void setParam(String clsRoot, String reloadPkg) {
		this.reloadPkg = reloadPkg;
	}

	public int getGeneration() {
		return generation.get();
	}

	/**
	 * 强制换一代（运维/测试用）。正常由后台线程触发。
	 */
	public synchronized void newGeneration() {
		String r = resolveRoot();
		if (r == null) {
			return;
		}
		int gen = generation.incrementAndGet();
		loaderVersion = gen;
		// 先清缓存，再切换；否则请求可能拿到"新 loader + 旧 Method"
		MethodAccessCacher.clear();
		CallCacheMap.clear();
		Loader.clearSingletons();
		YangClassLoader l = new YangClassLoader(gen, r, reloadPkg);
		current = l;
		loader = l;
		pendingChangeAt = 0;
		dirty = false;
		logger.info("hot reload: generation {} , class root = {}", gen, r);
	}

	private YangClassLoader loader() {
		YangClassLoader l = current;
		if (l == null) {
			synchronized (this) {
				l = current;
				if (l == null) {
					newGeneration();
					l = current;
				}
			}
		}
		return l;
	}

	public Class<?> load(String clsName) throws ClassNotFoundException {
		if (!Config.reloadable) {
			Class<?> ret = Class.forName(clsName);
			MethodAccessCacher.putIfNoExist(clsName, ret);
			return ret;
		}
		ensureWatcher();
		if (Config.reload_scan_interval_ms <= 0) {
			// 没有后台线程时，退化为"请求时检查当前这个类"（老行为，但换代仍是整代）
			checkRequestedClass(clsName);
		}
		YangClassLoader l = loader();
		if (l == null) {
			Class<?> ret = Class.forName(clsName);
			MethodAccessCacher.putIfNoExist(clsName, ret);
			return ret;
		}
		Class<?> c = l.loadClass(clsName);
		// 换代期间可能刚好换了 loader：那就先不记缓存，下一次请求会重新定义
		if (l == current) {
			MethodAccessCacher.remember(clsName, c);
		}
		return c;
	}

	/**
	 * 兼容旧 API：返回当前代的加载器。
	 */
	public ClassLoader classIsUpdate(String clsName) {
		if (Config.reload_scan_interval_ms <= 0) {
			checkRequestedClass(clsName);
		}
		return loader();
	}

	/**
	 * 兼容旧 API：手动扫一次。
	 */
	public void scan(String reloadDir) {
		scanOnce();
	}

	protected String clsToPath(String clsName) {
		String r = resolveRoot();
		if (r == null) {
			r = declaredRoot == null ? "" : declaredRoot;
		}
		if (!r.endsWith("/")) {
			r = r + "/";
		}
		return r + clsName.replace(".", "/") + ".class";
	}

	protected void T(String s) {
		logger.debug(s);
	}

	// ------------------------------------------------------------------
	// 目录解析 / 扫描 / 换代
	// ------------------------------------------------------------------

	/**
	 * 解析 class 文件目录：class_root 配置 → bin/ → target/classes/ → CodeSource。
	 * 解析不到（比如打成了 fat jar）返回 null，此时热加载自动失效。
	 */
	private String resolveRoot() {
		if (rootResolved) {
			return root;
		}
		synchronized (this) {
			if (rootResolved) {
				return root;
			}
			root = doResolveRoot();
			rootResolved = true;
			if (root == null) {
				logger.warn("hot reload disabled: can not locate the class directory (set class_root in web.properties)");
			}
			return root;
		}
	}

	private String doResolveRoot() {
		String pkgPath = (reloadPkg == null ? Config.controller : reloadPkg);
		pkgPath = pkgPath == null ? "" : pkgPath.replace('.', '/');

		String cfg = Config.class_root;
		if (cfg != null && !cfg.trim().isEmpty()) {
			File f = new File(cfg.trim());
			if (f.isDirectory()) {
				return f.getAbsolutePath();
			}
			logger.warn("class_root [{}] is not a directory, fallback to auto detect", cfg);
		}

		String[] candidates = { "bin", "target/classes", "WebRoot/WEB-INF/classes" };
		for (String c : candidates) {
			File dir = new File(c);
			if (!dir.isDirectory()) {
				continue;
			}
			if (pkgPath.isEmpty() || new File(dir, pkgPath).isDirectory()) {
				logger.info("hot reload: use class root [{}]", dir.getAbsolutePath());
				return dir.getAbsolutePath();
			}
		}

		try {
			CodeSource cs = Reloader.class.getProtectionDomain().getCodeSource();
			if (cs != null) {
				URL url = cs.getLocation();
				if (url != null && "file".equals(url.getProtocol())) {
					File f = new File(url.toURI());
					if (f.isDirectory()) {
						logger.info("hot reload: use class root from CodeSource [{}]", f.getAbsolutePath());
						return f.getAbsolutePath();
					}
				}
			}
		} catch (Throwable t) {
			logger.debug("can not resolve class root from CodeSource: {}", t.toString());
		}
		return null;
	}

	private void ensureWatcher() {
		if (watcherStarted || Config.reload_scan_interval_ms <= 0) {
			return;
		}
		synchronized (this) {
			if (watcherStarted) {
				return;
			}
			if (resolveRoot() == null) {
				watcherStarted = true; // 解析不到就别反复尝试
				return;
			}
			watcherStarted = true;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					watchLoop();
				}
			}, "yangmvc-reload-watcher");
			t.setDaemon(true);
			t.start();
			logger.info("hot reload: watcher started, interval={}ms, debounce={}ms, exclude=[{}]",
					Config.reload_scan_interval_ms, Config.reload_debounce_ms, Config.reload_exclude_packages);
		}
	}

	private void watchLoop() {
		while (Config.reloadable) {
			try {
				Thread.sleep(Math.max(50, Config.reload_scan_interval_ms));
			} catch (InterruptedException e) {
				return;
			}
			try {
				scanOnce();
			} catch (Throwable t) {
				logger.warn("hot reload scan failed, will retry next round", t);
			}
		}
		logger.info("hot reload: watcher stopped (reloadable=false)");
	}

	/**
	 * 扫一次目录：发现变化后按防抖窗口换代。
	 */
	void scanOnce() {
		String r = resolveRoot();
		if (r == null || reloadPkg == null) {
			return;
		}
		File base = new File(r);
		List<File> files = new ArrayList<File>();
		walk(base, files, 0);

		for (File f : files) {
			String cn = fileToClass(r, f);
			if (cn == null || !ReloadFilter.isReloadable(cn, reloadPkg)) {
				continue;
			}
			long tm = f.lastModified();
			Long old = last_tm.put(cn, tm);
			if (old != null && tm > old) {
				logger.info("hot reload: class changed [{}]", cn);
				dirty = true;
				pendingChangeAt = System.currentTimeMillis();
			}
		}
		// 被排除的包（service/pojo）也变了的话，提示要重启
		if (!dirty) {
			logIgnoredChanges(r, files);
		}

		maybeBump();
	}

	private void maybeBump() {
		if (!dirty) {
			return;
		}
		if (System.currentTimeMillis() - pendingChangeAt >= Math.max(0, Config.reload_debounce_ms)) {
			newGeneration();
		}
	}

	private void logIgnoredChanges(String r, List<File> files) {
		for (File f : files) {
			String cn = fileToClass(r, f);
			if (cn == null || !cn.startsWith(reloadPkg + ".")) {
				continue;
			}
			if (ReloadFilter.isReloadable(cn, reloadPkg)) {
				continue;
			}
			long tm = f.lastModified();
			Long old = last_tm.get("!" + cn);
			if (old == null) {
				last_tm.put("!" + cn, tm);
			} else if (tm > old) {
				last_tm.put("!" + cn, tm);
				logger.info("hot reload: ignored change in excluded package (restart required): {}", cn);
			}
		}
	}

	private void walk(File dir, List<File> out, int depth) {
		if (dir == null || depth > 32) {
			return;
		}
		File[] children = dir.listFiles();
		if (children == null) {
			return;
		}
		for (File c : children) {
			if (c.isDirectory()) {
				walk(c, out, depth + 1);
			} else if (c.getName().endsWith(".class")) {
				out.add(c);
			}
		}
	}

	private String fileToClass(String baseDir, File f) {
		String base = baseDir.replace('\\', '/');
		if (!base.endsWith("/")) {
			base = base + "/";
		}
		String path = f.getAbsolutePath().replace('\\', '/');
		if (!path.startsWith(base)) {
			return null;
		}
		String cn = path.substring(base.length());
		if (cn.endsWith(".class")) {
			cn = cn.substring(0, cn.length() - ".class".length());
		}
		return cn.replace('/', '.');
	}

	/**
	 * 没有后台线程时的兼容路径：只检查当前请求的这个类。
	 */
	private void checkRequestedClass(String clsName) {
		String r = resolveRoot();
		if (r == null) {
			return;
		}
		File f = new File(clsToPath(clsName));
		if (!f.isFile()) {
			return;
		}
		long tm = f.lastModified();
		Long old = last_tm.put(clsName, tm);
		if (old != null && tm > old) {
			logger.info("hot reload: class changed [{}]", clsName);
			dirty = true;
			pendingChangeAt = System.currentTimeMillis();
		}
		maybeBump();
	}

	// ------------------------------------------------------------------
	// 兼容入口
	// ------------------------------------------------------------------

	public static void main(String[] args) throws InterruptedException {
		Reloader reload = new Reloader("bin", "org");
		while (true) {
			reload.scanOnce();
			Thread.sleep(1000);
		}
	}

	/**
	 * 兼容入口：单独起一个监测线程（不依赖 MVC 初始化）。
	 */
	public static void startThread(String reloadDir) {
		final Reloader reload = new Reloader(reloadDir, Config.controller);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						reload.scanOnce();
					} catch (Exception e) {
						logger.warn("Reloader thread exception, retry 30s later", e);
						try {
							Thread.sleep(30 * 1000);
						} catch (InterruptedException e1) {
							return;
						}
					}
					try {
						Thread.sleep(Math.max(200, Config.reload_scan_interval_ms));
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, "yangmvc-reload-watcher");
		t.setDaemon(true);
		t.start();
	}
}
