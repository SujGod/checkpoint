package com.claimsift.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ClaimSiftBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimSiftBackendApplication.class, args);
	}

}
