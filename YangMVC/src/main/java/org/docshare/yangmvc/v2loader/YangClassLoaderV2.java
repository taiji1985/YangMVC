package org.docshare.yangmvc.v2loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;


public class YangClassLoaderV2 {
    // 基础包路径
    private final String basePackagePath;
    // 当前活动的类加载器
    private URLClassLoader currentClassLoader;
    // 上次加载时间
    private long lastLoadTime = 0;
    // 父类加载器
    private final ClassLoader parentClassLoader;

    public YangClassLoaderV2(String basePackagePath) {
        this.basePackagePath = basePackagePath;
        this.parentClassLoader = getClass().getClassLoader();
        this.currentClassLoader = createNewClassLoader();
    }

    

    // 加载类，如果需要则重新加载整个层次结构
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        try {
            // 检查类文件是否存在
            Path classPath = getClassPath(className);
            if (classPath == null || !Files.exists(classPath)) {
                return parentClassLoader.loadClass(className);
            }

            // 获取类文件的最后修改时间
            long lastModified = Files.getLastModifiedTime(classPath).toMillis();
            
            // 如果类已修改或首次加载，重新加载整个层次结构
            if (lastModified > lastLoadTime) {
                System.out.println("recreate classloader ,lastModified, " + lastModified +" ,lastLoadTime"+lastLoadTime);
                synchronized (this) {
                    // 双重检查锁
                    if (lastModified > lastLoadTime) {
                        currentClassLoader = createNewClassLoader();
                        lastLoadTime = System.currentTimeMillis();
                    }
                }
            }
            
            // 使用当前活动的类加载器加载类
            return currentClassLoader.loadClass(className);
        } catch (Exception e) {
            System.err.println("Failed to load class " + className + ": " + e.getMessage());
            return parentClassLoader.loadClass(className);
        }
    }

    // 创建新的类加载器
    private URLClassLoader createNewClassLoader() {
        try {
            URL[] urls = new URL[]{new File(basePackagePath).toURI().toURL()};
            return new CustomURLClassLoader(urls, parentClassLoader);
        } catch (Exception e) {
            e.printStackTrace();
            return (URLClassLoader) parentClassLoader;
        }
    }
    
    // 自定义类加载器，允许跳过父类委托
    private static class CustomURLClassLoader extends URLClassLoader {
        public CustomURLClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            // 对于我们要热重载的类，直接使用findClass，跳过父类委托
            if (name.startsWith("org.docshare.yangmvc.v2loader")) {
                try {
                    return findClass(name);
                } catch (ClassNotFoundException e) {
                    // 如果找不到，再委托给父类加载器
                    return super.loadClass(name);
                }
            }
            // 其他类使用正常的委托机制
            return super.loadClass(name);
        }
    }

    // 获取类的实例
    public Object getInstance(String className) throws Exception {
        Class<?> clazz = loadClass(className);
        return clazz.getDeclaredConstructor().newInstance();
    }

    // 获取类文件路径
    private Path getClassPath(String className) {
        String classFilePath = className.replace('.', File.separatorChar) + ".class";
        Path path = Paths.get(basePackagePath, classFilePath);
        return Files.exists(path) ? path : null;
    }


    // 测试main方法
    public static void main(String[] args) {
        System.out.println("=== YangClassLoaderV2 测试开始 ===");
        
        try {
            // 创建类加载器实例，使用当前项目的classes目录
            String basePath = "YangMVC/target/classes"; // Maven项目的编译输出目录
            YangClassLoaderV2 classLoader = new YangClassLoaderV2(basePath);
            
            System.out.println("类加载器创建成功，基础路径: " + basePath);
            
            // 测试加载Java标准库类
            System.out.println("\n--- 测试加载标准库类 ---");
            Class<?> stringClass = classLoader.loadClass("java.lang.String");
            System.out.println("成功加载类: " + stringClass.getName());
            
            System.out.println("\n--- 测试创建Demo实例并调用action方法 ---");
            Object instance = classLoader.getInstance("org.docshare.yangmvc.v2loader.Demo");
            System.out.println("成功创建实例: " + instance.getClass().getName());
            
            // 第一次调用action方法
            System.out.println("\n--- 第一次调用action方法 ---");
            DBase dbase = (DBase) instance;
            dbase.action();
            
            // 等待用户输入
            System.out.println("\n请按回车键继续进行第二次调用...");
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            
            // 第二次调用action方法
            System.out.println("\n--- 第二次调用action方法 ---");
            instance = classLoader.getInstance("org.docshare.yangmvc.v2loader.Demo");
            dbase = (DBase) instance;
            dbase.action();
            
            scanner.close();
            
            System.out.println("\n=== 所有测试完成 ===");
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}