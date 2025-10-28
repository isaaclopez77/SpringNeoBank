package com.springneobank.api_gateway.setups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class GlobalPostFiltering {

    private static final Logger log = LoggerFactory.getLogger(GlobalPreFiltering.class);

    @Bean
    public GlobalFilter GlobalPostFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    log.info("Global PostFilter executed");
                }));
        };
    }
}
