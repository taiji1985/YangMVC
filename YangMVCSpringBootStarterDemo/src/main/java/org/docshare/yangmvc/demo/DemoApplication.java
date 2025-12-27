package org.docshare.yangmvc.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * YangMVC Spring Boot Starter Demo应用
 * <p>
 * 演示如何使用yangmvc-spring-boot-starter
 * <p>
 * 启动后访问：
 * - http://localhost:8080/
 * - http://localhost:8080/hello/index
 * - http://localhost:8080/user/list
 * - http://localhost:8080/api/test
 *
 * @author YangMVC
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("YangMVC Spring Boot Starter Demo Started!");
        System.out.println("========================================");
        System.out.println("访问以下URL测试:");
        System.out.println("  - http://localhost:8080/");
        System.out.println("  - http://localhost:8080/hello/index");
        System.out.println("  - http://localhost:8080/user/list");
        System.out.println("  - http://localhost:8080/api/test");
        System.out.println("  - http://localhost:8080/h2-console (H2控制台)");
        System.out.println("========================================\n");
    }
}
