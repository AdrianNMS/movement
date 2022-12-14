package com.bank.movement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MovementApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovementApplication.class, args);
	}

}
