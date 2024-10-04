package com.appcenter.BJJ;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(servers = {
		@Server(url = "https://bjj.inuappcenter.kr", description = "Default Server URL")
})
@SpringBootApplication
public class BjjApplication {
	public static void main(String[] args) {
		SpringApplication.run(BjjApplication.class, args);
	}
}
