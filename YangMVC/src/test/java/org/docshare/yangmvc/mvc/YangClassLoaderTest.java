package org.docshare.yangmvc.mvc;

import org.junit.Test;
import static org.junit.Assert.*;

public class YangClassLoaderTest {
    
    @Test
    public void testClassLoaderBasicFunctionality() {
        System.out.println("=== 测试YangClassLoader基本功能 ===");
        
        // 创建自定义类加载器
        YangClassLoader loader = new YangClassLoader(1, "/test/classes/", "org.docshare.yangmvc");
        
        try {
            // 测试加载系统类（应该委托给父类加载器）
            Class<?> stringClass = loader.loadClass("java.lang.String");
            assertNotNull("应该能够加载系统类", stringClass);
            assertEquals("系统类应该由系统类加载器加载", 
                ClassLoader.getSystemClassLoader().getClass(), 
                stringClass.getClassLoader().getClass());
            
            System.out.println("✓ 系统类加载测试通过");
            
            // 测试重复加载同一个类
            Class<?> stringClass2 = loader.loadClass("java.lang.String");
            assertSame("重复加载应该返回同一个类对象", stringClass, stringClass2);
            
            System.out.println("✓ 类缓存测试通过");
            
        } catch (ClassNotFoundException e) {
            fail("不应该抛出ClassNotFoundException: " + e.getMessage());
        }
    }
    
    @Test
    public void testClassLoaderSynchronization() {
        System.out.println("=== 测试YangClassLoader并发安全性 ===");
        
        YangClassLoader loader = new YangClassLoader(2, "/test/classes/", "org.docshare.yangmvc");
        
        // 创建多个线程同时加载同一个类
        Thread[] threads = new Thread[5];
        Class<?>[] results = new Class<?>[5];
        
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    results[index] = loader.loadClass("java.lang.Object");
                } catch (ClassNotFoundException e) {
                    fail("线程" + index + "加载类失败: " + e.getMessage());
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                fail("线程等待被中断: " + e.getMessage());
            }
        }
        
        // 验证所有线程加载的是同一个类对象
        for (int i = 1; i < results.length; i++) {
            assertSame("所有线程应该加载同一个类对象", results[0], results[i]);
        }
        
        System.out.println("✓ 并发安全性测试通过");
    }
    
    @Test
    public void testJDKCompatibility() {
        System.out.println("=== 测试JDK兼容性 ===");
        
        // 打印当前JDK版本信息
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        System.out.println("当前JDK版本: " + javaVersion + " (" + javaVendor + ")");
        
        YangClassLoader loader = new YangClassLoader(3, "/test/classes/", "org.docshare.yangmvc");
        
        try {
            // 测试加载不同类型的系统类
            String[] systemClasses = {
                "java.lang.String",
                "java.util.ArrayList",
                "java.io.InputStream",
                "java.net.URL"
            };
            
            for (String className : systemClasses) {
                Class<?> clazz = loader.loadClass(className);
                assertNotNull("应该能够加载类: " + className, clazz);
                assertEquals("类名应该匹配", className, clazz.getName());
            }
            
            System.out.println("✓ JDK兼容性测试通过");
            
        } catch (ClassNotFoundException e) {
            fail("JDK兼容性测试失败: " + e.getMessage());
        }
    }
}