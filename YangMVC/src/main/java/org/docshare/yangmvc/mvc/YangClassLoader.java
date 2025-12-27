package org.docshare.yangmvc.mvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class YangClassLoader extends ClassLoader{
	private static final Logger logger = LoggerFactory.getLogger(YangClassLoader.class);

    private int version;
	private Object root;
	private String reloadPackage;
	public YangClassLoader(int v,String clsRoot,String reloadPackage) {
    	this.version = v;
    	if(! clsRoot.endsWith("/")){
    		clsRoot+= "/";
    	}
    	this.root = clsRoot;
    	this.reloadPackage = reloadPackage;
	}
	public String name2Path(String cname){
		return root + cname.replace(".", "/")+".class";
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, true);
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// 同步加载过程，避免并发问题
		synchronized (getClassLoadingLock(name)) {
			// 首先检查类是否已经加载
			Class<?> loadedClass = findLoadedClass(name);
			if (loadedClass != null) {
				if (resolve) {
					resolveClass(loadedClass);
				}
				return loadedClass;
			}

			// 检查是否属于需要重新加载的包
			if (!name.startsWith(reloadPackage)) {
				// 使用父类加载器加载非指定包的类
				try {
					logger.debug("YangClassLoader use Parent Loader "+getParent().getClass().getName()+" -> "+name);

					Class<?> parentClass = getParent().loadClass(name);
					if (resolve) {
						resolveClass(parentClass);
					}
					return parentClass;
				} catch (ClassNotFoundException e) {
					// 如果父类加载器无法加载，继续尝试自定义加载
				}
			}

			// 尝试使用findClass方法加载类
			try {
				Class<?> clazz = findClass(name);
				if (resolve) {
					resolveClass(clazz);
				}
				return clazz;
			} catch (ClassNotFoundException e) {
				// 如果自定义加载失败，委托给父类加载器
				if (name.startsWith(reloadPackage)) {
					Class<?> parentClass = getParent().loadClass(name);
					if (resolve) {
						resolveClass(parentClass);
					}
					return parentClass;
				}
				throw e;
			}
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		logger.debug("YangClassLoader v" + version + ", finding " + name);

		// 加载类字节码
		String path = name2Path(name);
		try (InputStream input = getClass().getResourceAsStream(path)) {
			if (input == null) {
				logger.debug("YangClassLoader can not find class file: " + path);
				throw new ClassNotFoundException("Class file not found: " + path);
			}

			// 读取类字节码
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] buf = new byte[10240];
			int num;
			while ((num = input.read(buf)) != -1) {
				buffer.write(buf, 0, num);
			}
			byte[] classData = buffer.toByteArray();

			logger.debug("YangClassLoader defining class: " + name);
			
			// 添加类转换调试信息
			if (classData.length > 0) {
				logger.debug("Class data size: " + classData.length + " bytes");
			} else {
				logger.warn("Warning: Empty class data for " + name);
				throw new ClassNotFoundException("Empty class data for " + name);
			}
			
			// 定义类 - 使用更安全的方式
			Class<?> definedClass;
			try {
				// 为新版JDK提供更好的兼容性
				definedClass = defineClass(name, classData, 0, classData.length, getClass().getProtectionDomain());
			} catch (SecurityException e) {
				logger.warn("Security exception with ProtectionDomain, trying without: " + e.getMessage());
				// 如果使用ProtectionDomain失败，尝试不使用
				definedClass = defineClass(name, classData, 0, classData.length);
			}
			
			logger.debug("YangClassLoader loaded class: " + name);
			
			// 缓存类访问信息
			MethodAccessCacher.asyncPut(name, definedClass);
			logger.debug("YangClassLoader cached class: " + name);
			
			return definedClass;
		} catch (IOException e) {
			logger.error("Error loading class " + name + ": " + e.getMessage());
			throw new ClassNotFoundException("IO error loading class " + name, e);
		} catch (SecurityException | ClassFormatError e) {
			logger.error("Error defining class " + name + ": " + e.getMessage());
			throw new ClassNotFoundException("Error defining class " + name, e);
		}
	}
}
