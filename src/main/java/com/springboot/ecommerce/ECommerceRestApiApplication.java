package com.springboot.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ECommerceRestApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceRestApiApplication.class, args);
	}

}
