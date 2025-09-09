package com.beyond.HanSoom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HanSoomApplication {

	public static void main(String[] args) {
		SpringApplication.run(HanSoomApplication.class, args);
	}

}
