package com.musiccatalog.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.musiccatalog.dto.AnalyticsSummary;
import com.musiccatalog.dto.SearchResultDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                MediaType.valueOf("text/javascript"),
                MediaType.valueOf("text/javascript;charset=utf-8")
        ));
        restTemplate.getMessageConverters().add(0, converter);
        return restTemplate;
    }

    @Bean
    public Cache<String, List<SearchResultDto>> searchCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, AnalyticsSummary> analyticsCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats()
                .build();
    }
}