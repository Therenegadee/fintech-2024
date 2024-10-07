package ru.tbank.aop.logging.starter.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class MethodExecutionTimeLoggedAspect {
    @Around("execution(* *(..)) && (@within(ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked) " +
            "|| @annotation(ru.tbank.aop.logging.starter.annotation.MethodExecutionTimeTracked))")
    public Object profileAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        final StopWatch stopWatch = new StopWatch();

        stopWatch.start();
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();

        String logDelimiter = "-".repeat(100);
        log.info(logDelimiter);
        log.info("Выполнение метода {}#{} заняло: {} ms.", className, methodName, stopWatch.getTotalTimeMillis());
        log.info(logDelimiter);
        return result;
    }
}
