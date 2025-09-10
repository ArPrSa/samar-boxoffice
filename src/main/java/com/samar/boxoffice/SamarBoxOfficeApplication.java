package com.samar.boxoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SamarBoxOfficeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SamarBoxOfficeApplication.class, args);
	}

}
