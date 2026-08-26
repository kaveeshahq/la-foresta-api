package com.laforesta.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LaforestaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LaforestaApiApplication.class, args);
	}

}
