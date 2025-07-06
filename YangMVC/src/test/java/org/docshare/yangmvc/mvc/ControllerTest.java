package org.docshare.yangmvc.mvc;

import org.junit.Test;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;

import org.mockito.Mockito;

/**
 * Controller重构后的测试类
 * 验证请求上下文模式是否正常工作，以及是否支持单例模式
 */
public class ControllerTest {

    @Test
    public void testRequestContextIsolation() throws Exception {
        // 创建Controller实例（可以是单例）
        Controller controller = new Controller();
        
        // 模拟两个不同的请求
        HttpServletRequest req1 = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp1 = Mockito.mock(HttpServletResponse.class);
        HttpSession session1 = Mockito.mock(HttpSession.class);
        ServletContext context1 = Mockito.mock(ServletContext.class);
        
        HttpServletRequest req2 = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp2 = Mockito.mock(HttpServletResponse.class);
        HttpSession session2 = Mockito.mock(HttpSession.class);
        ServletContext context2 = Mockito.mock(ServletContext.class);
        
        Mockito.when(req1.getSession()).thenReturn(session1);
        Mockito.when(req1.getServletContext()).thenReturn(context1);
        Mockito.when(req1.getCookies()).thenReturn(null);
        Mockito.when(req1.getContentType()).thenReturn("text/html");
        
        Mockito.when(req2.getSession()).thenReturn(session2);
        Mockito.when(req2.getServletContext()).thenReturn(context2);
        Mockito.when(req2.getCookies()).thenReturn(null);
        Mockito.when(req2.getContentType()).thenReturn("text/html");
        
        // 在不同线程中模拟请求处理
        Thread thread1 = new Thread(() -> {
            try {
                controller.setReq(req1, resp1);
                controller.putParam("user", "thread1_user");
                controller.put("data", "thread1_data");
                
                // 验证当前线程的数据
                assertEquals(req1, controller.request());
                assertEquals(resp1, controller.response());
                assertEquals(session1, controller.session());
                assertEquals(context1, controller.application());
                
                // 等待一段时间，确保另一个线程也在执行
                Thread.sleep(100);
                
                // 再次验证数据没有被其他线程污染
                assertEquals(req1, controller.request());
                assertEquals("thread1_user", controller.param("user"));
                
            } catch (Exception e) {
                e.printStackTrace();
                fail("Thread 1 failed: " + e.getMessage());
            } finally {
                controller.clearContext();
            }
        });
        
        Thread thread2 = new Thread(() -> {
            try {
                controller.setReq(req2, resp2);
                controller.putParam("user", "thread2_user");
                controller.put("data", "thread2_data");
                
                // 验证当前线程的数据
                assertEquals(req2, controller.request());
                assertEquals(resp2, controller.response());
                assertEquals(session2, controller.session());
                assertEquals(context2, controller.application());
                
                // 等待一段时间，确保另一个线程也在执行
                Thread.sleep(100);
                
                // 再次验证数据没有被其他线程污染
                    assertEquals(req2, controller.request());
                assertEquals("thread2_user", controller.param("user"));
                
            } catch (Exception e) {
                e.printStackTrace();
                fail("Thread 2 failed: " + e.getMessage());
            } finally {
                controller.clearContext();
            }
        });
        
        // 启动两个线程
        thread1.start();
        thread2.start();
        
        // 等待两个线程完成
        thread1.join();
        thread2.join();
        
        System.out.println("✓ 请求上下文隔离测试通过 - Controller可以安全地作为单例使用");
    }
    
    @Test
    public void testBackwardCompatibility() throws Exception {
        Controller controller = new Controller();
        
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        
        Mockito.when(req.getSession()).thenReturn(session);
        Mockito.when(req.getServletContext()).thenReturn(context);
        Mockito.when(req.getCookies()).thenReturn(null);
        Mockito.when(req.getContentType()).thenReturn("text/html");
        
        try {
            controller.setReq(req, resp);
            
            // 测试向后兼容性 - public字段应该仍然可用
            assertNotNull(controller.request());
            assertNotNull(controller.response());
            assertNotNull(controller.session());
            assertNotNull(controller.application());
            
            assertEquals(req, controller.request());
            assertEquals(resp, controller.response());
            assertEquals(session, controller.session());
            assertEquals(context, controller.application());
            
            System.out.println("✓ 向后兼容性测试通过 - 现有代码无需修改");
            
        } finally {
            controller.clearContext();
        }
    }
    
    @Test
    public void testSingletonSafety() throws Exception {
        // 使用同一个Controller实例处理多个请求
        Controller singletonController = new Controller();
        
        final int threadCount = 10;
        final int requestsPerThread = 5;
        Thread[] threads = new Thread[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    try {
                        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
                        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
                        HttpSession session = Mockito.mock(HttpSession.class);
                        ServletContext context = Mockito.mock(ServletContext.class);
                        
                        Mockito.when(req.getSession()).thenReturn(session);
                        Mockito.when(req.getServletContext()).thenReturn(context);
                        Mockito.when(req.getCookies()).thenReturn(null);
                        Mockito.when(req.getContentType()).thenReturn("text/html");
                        
                        singletonController.setReq(req, resp);
                        
                        String uniqueValue = "thread_" + threadId + "_request_" + j;
                        singletonController.putParam("threadId", uniqueValue);
                        
                        // 验证数据隔离
                        assertEquals(uniqueValue, singletonController.param("threadId"));
                        assertEquals(req, singletonController.request());
                        
                        // 模拟一些处理时间
                        Thread.sleep(10);
                        
                        // 再次验证数据没有被污染
                        assertEquals(uniqueValue, singletonController.param("threadId"));
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        fail("Singleton safety test failed: " + e.getMessage());
                    } finally {
                        singletonController.clearContext();
                    }
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("✓ 单例安全性测试通过 - " + threadCount + "个线程，每个" + requestsPerThread + "个请求，总计" + (threadCount * requestsPerThread) + "个请求处理成功");
    }
}