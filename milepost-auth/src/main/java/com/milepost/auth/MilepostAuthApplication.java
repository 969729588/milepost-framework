package com.milepost.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MilepostAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(MilepostAuthApplication.class, args);
	}

}
