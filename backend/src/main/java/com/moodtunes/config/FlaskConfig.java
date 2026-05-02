package com.moodtunes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for the Flask AI microservice client.
 *
 * Properties (set in application.properties; defaults shown):
 *   flask.base-url=http://localhost:5001
 *   flask.connect-timeout-ms=3000
 *   flask.read-timeout-ms=30000   // Gemini + ytmusicapi can be slow
 *
 * Owner: Sid Goyal
 */
@Configuration
public class FlaskConfig {

    @Value("${flask.base-url:http://localhost:5001}")
    private String baseUrl;

    @Value("${flask.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${flask.read-timeout-ms:30000}")
    private int readTimeoutMs;

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    /**
     * RestTemplate dedicated to talking to the Flask service.
     * Named so other beans don't accidentally pick it up if they ever
     * need a different HTTP client.
     */
    @Bean(name = "flaskRestTemplate")
    public RestTemplate flaskRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
