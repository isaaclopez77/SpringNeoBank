package com.springneobank.api_gateway.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

// -----------  THIS CONTROLLER EXPOSES EVERY MICROSERVICE SWAGGER JSON IN /docs/microservicename ---------- //

@RestController
@RequestMapping("/docs")
public class SwaggerController {

    @Autowired
    private WebClient.Builder webClient;

    @GetMapping("/auth")
    public Mono<String> auth() {
        return webClient.build()
                .get()
                .uri("http://businessdomain-auth/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/user")
    public Mono<String> user() {
        return webClient.build()
                .get()
                .uri("http://businessdomain-user/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class);
    }
}
