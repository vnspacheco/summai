package com.summai.api;

import com.summai.api.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SummaiApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SummaiApiApplication.class, args);
	}

}
