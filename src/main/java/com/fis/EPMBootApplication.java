package com.fis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

//@SpringBootApplication
@ComponentScan("com.fis.epm.*")
@EntityScan({"com.fis.epm.*","com.fis.pg.emp.*"})
@EnableAsync
@EnableSwagger2
@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class EPMBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(EPMBootApplication.class, "");
	}

}
