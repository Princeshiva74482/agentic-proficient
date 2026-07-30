package com.agenticproficient.urlshortner.config;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI urlShortenerOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Agentic URL Shortener API")
						.version("1.0.0")
						.description("Reactive URL shortener with analytics and governed agentic SDLC orchestration.")
						.contact(new Contact()
								.name("Engineering Review")
								.email("reviewer@example.com"))
						.license(new License()
								.name("Internal Assessment")))
				.servers(List.of(new Server()
						.url("http://localhost:8080")
						.description("Local development server")));
	}
}
