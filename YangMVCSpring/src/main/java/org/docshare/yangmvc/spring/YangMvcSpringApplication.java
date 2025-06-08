package org.docshare.yangmvc.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.docshare.yangmvc.spring.config.YangMvcProperties;

@SpringBootApplication
@EnableConfigurationProperties(YangMvcProperties.class)
public class YangMvcSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(YangMvcSpringApplication.class, args);
	}

}
