package ru.tbank.hw5.aspect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AspectConfiguration {

    @Bean
    public MethodExecutionTimeLoggingAspect methodExecutionTimeLoggingAspect() {
        return new MethodExecutionTimeLoggingAspect();
    }
}
