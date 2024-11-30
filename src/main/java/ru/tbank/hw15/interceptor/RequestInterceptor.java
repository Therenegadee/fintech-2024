package ru.tbank.hw15.interceptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.tbank.hw15.metrics.RequestCounterMetric;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Integer.MIN_VALUE)
@RequiredArgsConstructor
@Slf4j
public class RequestInterceptor extends OncePerRequestFilter {

    private final RequestCounterMetric requestCounterMetric;

    private static final String REQUEST_ID = "requestId";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        requestCounterMetric.incrementRequestCounter();
        log.info("Получен {} запрос по пути {}.", request.getMethod(), request.getServletPath());
        filterChain.doFilter(request, response);
    }
}
