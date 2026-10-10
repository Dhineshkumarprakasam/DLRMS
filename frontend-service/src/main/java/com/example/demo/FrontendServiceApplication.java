package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FrontendServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontendServiceApplication.class, args);
	}
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
				String cookie = attributes.getRequest().getHeader(HttpHeaders.COOKIE);
				if (cookie != null) {
					request.getHeaders().set(HttpHeaders.COOKIE, cookie);
				}
			}
			return execution.execute(request, body);
		});
		return restTemplate;
	}

}
