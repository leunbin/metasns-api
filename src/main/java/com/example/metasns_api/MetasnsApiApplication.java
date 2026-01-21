package com.example.metasns_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MetasnsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetasnsApiApplication.class, args);
	}

}
