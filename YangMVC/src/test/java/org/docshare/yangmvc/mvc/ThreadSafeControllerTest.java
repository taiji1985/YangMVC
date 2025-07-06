package org.docshare.yangmvc.mvc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * Controller线程安全性和新getter方法测试
 * 验证重构后的Controller是否支持单例模式和线程安全
 * 
 * @author 杨同峰
 */
public class ThreadSafeControllerTest {
    
    /**
     * 测试新的getter方法功能
     */
    @Test
    public void testNewGetterMethods() {
        System.out.println("=== 测试新的getter方法功能 ===");
        
        Controller controller = new Controller();
        
        // 创建模拟对象
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        ServletContext mockApplication = Mockito.mock(ServletContext.class);
        
        // 设置模拟对象的行为
        Mockito.when(mockRequest.getSession()).thenReturn(mockSession);
        Mockito.when(mockRequest.getServletContext()).thenReturn(mockApplication);
        Mockito.when(mockRequest.getParameter("test")).thenReturn("testValue");
        
        try {
            // 设置请求上下文
            controller.setReq(mockRequest, mockResponse);
            
            // 测试新的getter方法
            assertNotNull("request() should return valid HttpServletRequest", controller.request());
            assertNotNull("response() should return valid HttpServletResponse", controller.response());
            assertNotNull("session() should return valid HttpSession", controller.session());
            assertNotNull("application() should return valid ServletContext", controller.application());
            
            // 验证返回的对象是正确的
            assertEquals("request() should return the same request object", mockRequest, controller.request());
            assertEquals("response() should return the same response object", mockResponse, controller.response());
            assertEquals("session() should return the same session object", mockSession, controller.session());
            assertEquals("application() should return the same application object", mockApplication, controller.application());
            
            // 测试通过新方法访问请求参数
            String paramValue = controller.request().getParameter("test");
            assertEquals("Should be able to access request parameters via request()", "testValue", paramValue);
            
            System.out.println("✓ 新的getter方法测试通过");
            
        } finally {
            controller.clearContext();
        }
    }
    
    /**
     * 测试向后兼容性
     */
    @Test
    public void testBackwardCompatibility() {
        System.out.println("=== 测试向后兼容性 ===");
        
        Controller controller = new Controller();
        
        // 创建模拟对象
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        ServletContext mockApplication = Mockito.mock(ServletContext.class);
        
        Mockito.when(mockRequest.getSession()).thenReturn(mockSession);
        Mockito.when(mockRequest.getServletContext()).thenReturn(mockApplication);
        
        try {
            controller.setReq(mockRequest, mockResponse);
            
            // 验证新的getter方法返回正确的对象
            assertSame("request() should return the same request object", mockRequest, controller.request());
            assertSame("response() should return the same response object", mockResponse, controller.response());
            assertSame("session() should return the same session object", mockSession, controller.session());
            assertSame("application() should return the same application object", mockApplication, controller.application());
            
            System.out.println("✓ 向后兼容性测试通过");
            
        } finally {
            controller.clearContext();
        }
    }
    
    /**
     * 测试单例模式下的线程安全性
     */
    @Test
    public void testSingletonThreadSafety() throws Exception {
        System.out.println("=== 测试单例模式下的线程安全性 ===");
        
        final int THREAD_COUNT = 10;
        final Controller controller = new Controller(); // 单例Controller
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        
        final List<Controller> controllerInstances = new ArrayList<>();
        final List<RequestContext> requestContexts = new ArrayList<>();
        final List<String> threadResults = new ArrayList<>();
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>();
        
        // 启动多个线程同时访问同一个Controller实例
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            Future<?> future = executor.submit(() -> {
                try {
                    // 等待所有线程准备就绪
                    startLatch.await();
                    
                    // 创建每个线程独立的模拟对象
                    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
                    HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
                    HttpSession mockSession = Mockito.mock(HttpSession.class);
                    ServletContext mockApplication = Mockito.mock(ServletContext.class);
                    
                    Mockito.when(mockRequest.getSession()).thenReturn(mockSession);
                    Mockito.when(mockRequest.getServletContext()).thenReturn(mockApplication);
                    Mockito.when(mockRequest.getParameter("threadId")).thenReturn(String.valueOf(threadId));
                    
                    // 设置请求上下文
                    controller.setReq(mockRequest, mockResponse);
                    controller.putParam("testParam", "value" + threadId);
                    
                    // 记录Controller实例和请求上下文
                    synchronized (controllerInstances) {
                        controllerInstances.add(controller);
                        requestContexts.add(controller.getContext());
                    }
                    
                    // 验证每个线程都有独立的请求上下文（使用新的getter方法）
                    assertNotNull("Thread " + threadId + " should have request via request()", controller.request());
                    assertNotNull("Thread " + threadId + " should have response via response()", controller.response());
                    assertNotNull("Thread " + threadId + " should have session via session()", controller.session());
                    assertNotNull("Thread " + threadId + " should have application via application()", controller.application());
                    
                    // 验证参数隔离
                    String paramValue = controller.param("testParam");
                    String requestParam = controller.request().getParameter("threadId");
                    
                    synchronized (threadResults) {
                        threadResults.add("Thread " + threadId + ": param=" + paramValue + ", requestParam=" + requestParam);
                    }
                    
                    assertEquals("Thread " + threadId + " should have correct param", 
                                "value" + threadId, paramValue);
                    assertEquals("Thread " + threadId + " should have correct request param", 
                                String.valueOf(threadId), requestParam);
                    
                    // 模拟一些处理时间
                    Thread.sleep(10);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Thread " + threadId + " failed: " + e.getMessage());
                } finally {
                    controller.clearContext();
                    endLatch.countDown();
                }
            });
            futures.add(future);
        }
        
        // 启动所有线程
        startLatch.countDown();
        
        // 等待所有线程完成
        endLatch.await();
        executor.shutdown();
        
        // 验证所有线程都成功完成
        for (Future<?> future : futures) {
            future.get(); // 如果有异常会抛出
        }
        
        // 验证结果
        assertEquals("Should have " + THREAD_COUNT + " controller instances recorded", 
                    THREAD_COUNT, controllerInstances.size());
        assertEquals("Should have " + THREAD_COUNT + " request contexts recorded", 
                    THREAD_COUNT, requestContexts.size());
        
        // 验证单例安全性 - 多个线程应该使用同一个Controller实例
        assertTrue("All threads should use the same Controller instance", 
                  controllerInstances.stream().distinct().count() == 1);
        
        // 验证线程安全性 - 每个线程的请求上下文应该是独立的
        assertTrue("Each thread should have different request contexts", 
                  requestContexts.stream().distinct().count() == THREAD_COUNT);
        
        System.out.println("✓ 单例模式测试通过：" + THREAD_COUNT + "个线程使用同一个Controller实例");
        System.out.println("✓ 线程安全测试通过：每个线程都有独立的请求上下文");
        System.out.println("✓ 新的getter方法在多线程环境下工作正常");
        
        // 打印线程执行结果
        System.out.println("\n线程执行结果：");
        threadResults.forEach(System.out::println);
    }
    
    /**
     * 测试JSON和Writer方法
     */
    @Test
    public void testJsonAndWriterMethods() {
        System.out.println("=== 测试JSON和Writer方法 ===");
        
        Controller controller = new Controller();
        
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        
        try {
            Mockito.when(mockResponse.getWriter()).thenReturn(printWriter);
            Mockito.when(mockRequest.getSession()).thenReturn(Mockito.mock(HttpSession.class));
            Mockito.when(mockRequest.getServletContext()).thenReturn(Mockito.mock(ServletContext.class));
            
            controller.setReq(mockRequest, mockResponse);
            
            // 测试JSON方法
            controller.getContext().setJson("{\"test\":\"value\"}");
            String jsonData = controller.json();
            assertEquals("json() should return correct JSON data", "{\"test\":\"value\"}", jsonData);
            
            // 测试Writer方法
            controller.getContext().setWriter(printWriter);
            PrintWriter writer = controller.writer();
            assertNotNull("writer() should return valid PrintWriter", writer);
            
            writer.println("Test output");
            writer.flush();
            assertTrue("Writer should contain test output", stringWriter.toString().contains("Test output"));
            
            System.out.println("✓ JSON和Writer方法测试通过");
            
        } catch (Exception e) {
            fail("JSON and Writer test failed: " + e.getMessage());
        } finally {
            controller.clearContext();
        }
    }
    
    /**
     * 测试上下文清理
     */
    @Test
    public void testContextCleanup() {
        System.out.println("=== 测试上下文清理 ===");
        
        Controller controller = new Controller();
        
        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = Mockito.mock(HttpServletResponse.class);
        
        Mockito.when(mockRequest.getSession()).thenReturn(Mockito.mock(HttpSession.class));
        Mockito.when(mockRequest.getServletContext()).thenReturn(Mockito.mock(ServletContext.class));
        
        // 设置请求上下文
        controller.setReq(mockRequest, mockResponse);
        controller.putParam("test", "value");
        
        // 验证上下文已设置
        assertNotNull("Context should be set", controller.getContext());
        assertNotNull("Request should be available", controller.request());
        assertEquals("Param should be available", "value", controller.param("test"));
        
        // 清理上下文
        controller.clearContext();
        
        // 验证上下文已清理（新的getter方法应该返回null）
        assertNull("request() should return null after clearContext()", controller.request());
        assertNull("response() should return null after clearContext()", controller.response());
        assertNull("session() should return null after clearContext()", controller.session());
        assertNull("application() should return null after clearContext()", controller.application());
        assertNull("writer() should return null after clearContext()", controller.writer());
        assertNull("json() should return null after clearContext()", controller.json());
        
        System.out.println("✓ 上下文清理测试通过");
    }
}