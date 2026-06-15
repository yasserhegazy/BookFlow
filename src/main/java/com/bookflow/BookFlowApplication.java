package com.bookflow;

import com.bookflow.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class BookFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookFlowApplication.class, args);
	}

}
