package ru.tbank.hw15.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestCounterMetric {

    private final MeterRegistry meterRegistry;

    private static final String COUNTER_NAME = "request_counter";

    public Counter getRequestCounter() {
        return meterRegistry.counter(COUNTER_NAME);
    }

    public void incrementRequestCounter() {
        meterRegistry.counter(COUNTER_NAME).increment();
    }
}
