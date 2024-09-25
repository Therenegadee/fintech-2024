package ru.tbank.hw5.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class MethodExecutionTimeLoggingAspect {
    @Around("execution(* *(..)) && (@within(ru.tbank.hw5.annotation.ExecutionTimeObserved)" +
            "|| @annotation(ru.tbank.hw5.annotation.ExecutionTimeObserved)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long initTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - initTime;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = signature.getDeclaringTypeName();

        log.info("============================================================================================================");
        log.info("Class: {}, Method: {}", className, methodName);
        log.info("Method executed in: {} ms", executionTime);
        log.info("============================================================================================================");
        return proceed;
    }
}
