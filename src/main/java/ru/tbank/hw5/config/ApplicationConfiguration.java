package ru.tbank.hw5.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.tbank.hw5.exception.RestTemplateResponseErrorHandler;
import ru.tbank.hw5.interceptor.RestClientLoggingRequestInterceptor;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public RestTemplateResponseErrorHandler responseErrorHandler() {
        return new RestTemplateResponseErrorHandler();
    }

    @Bean
    public RestClientLoggingRequestInterceptor requestLoggingInterceptor() {
        return new RestClientLoggingRequestInterceptor();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .interceptors(requestLoggingInterceptor())
                .errorHandler(responseErrorHandler())
                .build();
    }
}
