package com.gameprice.comparator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder steamWebClientBuilder() {
        return WebClient.builder()
            .baseUrl("https://store.steampowered.com/api")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "GamePriceComparator/1.0");
    }

    @Bean
    public WebClient steamWebClient() {
        return WebClient.builder()
            .baseUrl("https://store.steampowered.com/api")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("User-Agent", "GamePriceComparator/1.0")
            .build();
    }
}
