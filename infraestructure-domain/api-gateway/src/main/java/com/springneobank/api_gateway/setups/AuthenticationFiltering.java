package com.springneobank.api_gateway.setups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFiltering extends AbstractGatewayFilterFactory<AuthenticationFiltering.Config> {

    private final WebClient.Builder wcBuilder;

    private static final Logger log = LoggerFactory.getLogger(GlobalPreFiltering.class);

    public AuthenticationFiltering(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.wcBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter((exchange, chain) -> {  
            // Check header
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
            }
             
             // Check bearer
            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String[] parts = authHeader.split(" ");
            if (parts.length != 2 || !"Bearer".equals(parts[0])) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad Authorization structure");
            }

            // Check token validity
            String token = parts[1];

            return wcBuilder.build()
                .get()
                .uri("http://businessdomain-auth/auth/validate_token")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                    clientResponse.bodyToMono(String.class)
                        .map(body -> new ResponseStatusException(
                            clientResponse.statusCode(),
                            "Token validation failed: " + body
                        ))
                )
                .toBodilessEntity() // No need for a body, just check status
                .flatMap(response -> {
                    log.info("Token validated successfully");
                    return chain.filter(exchange);
                })
                .doOnError(error -> log.error("Token validation failed", error));
            
            // Check roles
            /*return wcBuilder.build()
                .get()
                .uri("http://businessdomain-auth/roles").header(HttpHeaders.AUTHORIZATION, parts[1]) // Endpoint already validate the token                           
                .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(response -> {  
                        if(response != null){
                            log.info("See Objects: " + response);  
                            //check for Partners rol                                 
                            if(response.get("Partners") == null || StringUtils.isEmpty(response.get("Partners").asText())){
                                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Role Partners missing");
                            }
                        }else{
                            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Roles missing");
                        }
                    return exchange;
            })
            .onErrorMap(error -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Communication Error", error.getCause());})
            .flatMap(chain::filter); */
            
            
        },1);
    }

    public static class Config {

    }
}
