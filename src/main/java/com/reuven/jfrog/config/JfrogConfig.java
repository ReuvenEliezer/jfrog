package com.reuven.jfrog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class JfrogConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
