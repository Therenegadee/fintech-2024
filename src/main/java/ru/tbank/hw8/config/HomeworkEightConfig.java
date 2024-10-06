package ru.tbank.hw8.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;
import ru.tbank.hw8.interceptor.RestClientLoggingInterceptor;

@Configuration
@EnableCaching
public class HomeworkEightConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .messageConverters(configure -> configure.add(xmlMessageConverter()))
                .requestInterceptor(restClientLoggingInterceptor())
                .build();
    }

    @Bean
    public RestClientLoggingInterceptor restClientLoggingInterceptor() {
        return new RestClientLoggingInterceptor();
    }

    @Bean
    public HttpMessageConverter<Object> xmlMessageConverter() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MappingJackson2XmlHttpMessageConverter converter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
//        converter.setDefaultCharset("windows-1251");
        return converter;
    }
}
