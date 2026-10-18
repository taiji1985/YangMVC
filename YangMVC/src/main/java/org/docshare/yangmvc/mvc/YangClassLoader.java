package org.docshare.yangmvc.mvc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 热加载用的类加载器。
 *
 * <p>设计要点（都是踩过的坑）：
 * <ol>
 *   <li><b>parallel capable</b>：不注册的话 {@code getClassLoadingLock()} 返回 {@code this}，
 *       所有类加载全局串行，而且会与别的锁形成环路（历史上就是在这里卡死的）。</li>
 *   <li><b>findClass 里只读文件 + defineClass</b>：不调 {@code getResourceAsStream}（会去问父加载器）、
 *       不调 {@code getProtectionDomain()}、更不调 {@code MethodAccessCacher}（ReflectASM 内部会
 *       反过来加载类，形成锁顺序反转）。</li>
 *   <li>一个"代"一个 loader，由 {@link Reloader} 整代替换。</li>
 * </ol>
 */
class YangClassLoader extends ClassLoader {

	private static final Logger logger = LoggerFactory.getLogger(YangClassLoader.class);

	static {
		// 必须在类初始化时注册；否则 getClassLoadingLock(name) 退化成 this
		registerAsParallelCapable();
	}

	private final int version;
	private final String classRoot;
	private final String reloadPackage;

	public YangClassLoader(int v, String clsRoot, String reloadPackage) {
		// 显式指定 parent 为"框架所在 loader"：在 WAR/Tomcat 下比默认的 system loader 更正确
		super(YangClassLoader.class.getClassLoader());
		this.version = v;
		this.classRoot = normalizeDir(clsRoot);
		this.reloadPackage = reloadPackage;
	}

	/** 当前是第几代，用于排查"是不是换代了" */
	int getVersion() {
		return version;
	}

	private static String normalizeDir(String dir) {
		if (dir == null) {
			return null;
		}
		String d = dir.replace('\\', '/');
		while (d.endsWith("/") && d.length() > 1) {
			d = d.substring(0, d.length() - 1);
		}
		return d;
	}

	public String name2Path(String cname) {
		String root = classRoot == null ? "" : classRoot + "/";
		return root + cname.replace(".", "/") + ".class";
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {
			Class<?> loaded = findLoadedClass(name);
			if (loaded == null) {
				if (!ReloadFilter.isReloadable(name, reloadPackage)) {
					// 不在热加载范围（含 service/pojo 等排除项）→ 一律父加载器，保证只有一份
					loaded = getParent().loadClass(name);
				} else {
					try {
						loaded = findClass(name);
					} catch (ClassNotFoundException e) {
						// 目录里没有这个 class 文件时退回父加载器（兼容旧行为）
						loaded = getParent().loadClass(name);
					}
				}
			}
			if (resolve) {
				resolveClass(loaded);
			}
			return loaded;
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] data = readClassBytes(name);
		if (data == null) {
			throw new ClassNotFoundException("Class file not found: " + name2Path(name));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("YangClassLoader v{} define {} ({} bytes)", version, name, data.length);
		}
		return defineClass(name, data, 0, data.length);
	}

	/**
	 * 直接从磁盘读 class 字节。找不到返回 null。
	 */
	private byte[] readClassBytes(String name) {
		if (classRoot == null) {
			return null;
		}
		File f = new File(name2Path(name));
		if (!f.isFile()) {
			return null;
		}
		InputStream in = null;
		try {
			in = new FileInputStream(f);
			ByteArrayOutputStream bos = new ByteArrayOutputStream((int) Math.max(1024, f.length()));
			byte[] buf = new byte[8192];
			int n;
			while ((n = in.read(buf)) > 0) {
				bos.write(buf, 0, n);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			logger.warn("read class file failed: {}", f.getAbsolutePath(), e);
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {
				}
			}
		}
	}
}
