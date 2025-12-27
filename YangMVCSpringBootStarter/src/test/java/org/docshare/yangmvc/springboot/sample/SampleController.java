package org.docshare.yangmvc.springboot.sample;

import org.docshare.yangmvc.mvc.Controller;

/**
 * 示例控制器
 * <p>
 * 演示如何使用YangMVC的Controller
 *
 * @author YangMVC
 */
public class SampleController extends Controller {

    /**
     * 默认的index方法
     * <p>
     * 访问路径：/sample/index
     *
     * @return 返回字符串
     */
    public String index() {
        put("message", "Hello from YangMVC Spring Boot Starter!");
        put("status", "success");
        return "ok";
    }

    /**
     * 测试方法
     * <p>
     * 访问路径：/sample/test
     */
    public void test() {
        put("title", "YangMVC Spring Boot Starter Test");
        put("content", "This is a test page");
        render();
    }
}
