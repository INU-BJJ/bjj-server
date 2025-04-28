package com.appcenter.BJJ;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

//API의 기본 서버 url을 애플리케이션의 루트 경로로 설정
@OpenAPIDefinition(servers = {
        @Server(url = "/", description = "Default Server URL")
})
@EnableRetry
@EnableScheduling
@SpringBootApplication
public class BjjApplication {
    public static void main(String[] args) {
        SpringApplication.run(BjjApplication.class, args);
    }
}
