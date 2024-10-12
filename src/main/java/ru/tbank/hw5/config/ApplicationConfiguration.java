package ru.tbank.hw5.config;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.tbank.hw5.exception.RestTemplateResponseErrorHandler;
import ru.tbank.hw5.interceptor.RestClientLoggingRequestInterceptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class ApplicationConfiguration {

    @Value("${resources.available-cores}")
    private int numberOfAvailableCores;

    @Value("${kudago-api.statistics.avg-response-time-ms}")
    private int avgApiResponseTimeMs;

    @Value("${kudago-api.statistics.avg-processing-response-time-ms}")
    private int avgProcessingApiResponseTimeMs;

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
//        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
//                .namingPattern("FixedThreadPoolExecutor-%d")
//                .build();
        return Executors.newFixedThreadPool(calculateCorePoolSize(numberOfAvailableCores, avgApiResponseTimeMs, avgProcessingApiResponseTimeMs));
    }

    @Bean("kudaGoApiScheduledThreadPoolExecutorService")
    public ExecutorService scheduledThreadPoolExecutorService() {
//        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder()
//                .namingPattern("ScheduledThreadPoolExecutor-%d")
//                .build();
        return Executors.newScheduledThreadPool(calculateCorePoolSize(numberOfAvailableCores, avgApiResponseTimeMs, avgProcessingApiResponseTimeMs));
    }

    private int calculateCorePoolSize(int numberOfAvailableCores, int avgApiResponseTimeMsm, int avgProcessingApiResponseTimeMs) {
        return numberOfAvailableCores * (1 + avgApiResponseTimeMsm / avgProcessingApiResponseTimeMs);
    }
}
