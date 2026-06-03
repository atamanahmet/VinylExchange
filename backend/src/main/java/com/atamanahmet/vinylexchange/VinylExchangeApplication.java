package com.atamanahmet.vinylexchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VinylExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(VinylExchangeApplication.class, args);
	}

}
