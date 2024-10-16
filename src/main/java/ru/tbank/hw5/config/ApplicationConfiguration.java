package ru.tbank.hw5.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.tbank.hw5.exception.RestTemplateResponseErrorHandler;
import ru.tbank.hw5.interceptor.RestClientLoggingRequestInterceptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Configuration
@EnableAsync
@Slf4j
public class ApplicationConfiguration {

    @Value("${executors.threads-num.scheduled}")
    private int scheduledExecutorThreadsNumber;

    @Value("${executors.threads-num.fixed}")
    private int fixedExecutorThreadsNumber;

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

    @Bean("kudaGoApiFixedThreadPoolExecutorService")
    public ExecutorService fixedThreadPoolExecutorService() {
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("fixExecutor-%d")
                .build();
        return Executors.newFixedThreadPool(fixedExecutorThreadsNumber, threadFactory);
    }

    @Bean("kudaGoApiScheduledThreadPoolExecutorService")
    public ScheduledExecutorService scheduledThreadPoolExecutorService() {
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern("scheduleExecutor-%d")
                .build();
        return Executors.newScheduledThreadPool(scheduledExecutorThreadsNumber, threadFactory);
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Отправлен {} запрос по адресу {}.", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}
