package com.atamanahmet.vinylexchange;

import com.atamanahmet.vinylexchange.config.DotenvApplicationContextInitializer;
import com.atamanahmet.vinylexchange.infrastructure.payment.IyzicoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(IyzicoProperties.class)
public class VinylExchangeApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(VinylExchangeApplication.class);
		app.addInitializers(new DotenvApplicationContextInitializer());
		app.run(args);
	}

}
