package org.docshare.yangmvc.springboot;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.mvc.MVCFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * YangMVC自动配置测试类
 * <p>
 * 测试YangMVC Spring Boot Starter的自动配置功能
 *
 * @author YangMVC
 */
@SpringBootTest(classes = org.docshare.yangmvc.springboot.sample.SampleApplication.class)
@TestPropertySource(properties = {
    "yangmvc.controller=org.docshare.yangmvc.springboot.sample",
    "yangmvc.template=/view",
    "yangmvc.reloadable=true"
})
class YangMvcAutoConfigurationTest {

    @Autowired
    private ApplicationContext context;

    /**
     * 测试MVCFilter是否被正确注册
     */
    @Test
    void yangMvcFilterIsRegistered() {
        MVCFilter filter = context.getBean(MVCFilter.class);
        assertNotNull(filter, "MVCFilter should be registered in Spring context");
    }

    /**
     * 测试YangMVC配置是否被正确加载
     */
    @Test
    void configIsLoaded() {
        assertNotNull(Config.controller, "Controller package should be loaded");
        assertEquals("org.docshare.yangmvc.springboot.sample", Config.controller);
        assertNotNull(Config.template, "Template directory should be loaded");
        assertEquals("/view", Config.template);
        assertTrue(Config.reloadable, "Reloadable should be true");
    }

    /**
     * 测试SpringContextHolder是否被正确注册
     */
    @Test
    void springContextHolderIsRegistered() {
        assertNotNull(
            org.docshare.yangmvc.springboot.util.SpringContextHolder.getApplicationContext(),
            "SpringContextHolder should be initialized"
        );
    }

    /**
     * 测试FilterRegistrationBean是否被正确注册
     */
    @Test
    void filterRegistrationBeanIsRegistered() {
        org.springframework.boot.web.servlet.FilterRegistrationBean<?> filterRegistration =
            context.getBean(org.springframework.boot.web.servlet.FilterRegistrationBean.class);

        assertNotNull(filterRegistration, "FilterRegistrationBean should be registered");
        assertTrue(filterRegistration.getUrlPatterns().contains("/*"));
    }
}
