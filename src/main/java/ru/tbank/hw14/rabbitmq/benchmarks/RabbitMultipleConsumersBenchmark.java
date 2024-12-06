package ru.tbank.hw14.rabbitmq.benchmarks;

public class RabbitMultipleConsumersBenchmark extends RabbitMqBaseBenchmark {
    public RabbitMultipleConsumersBenchmark() {
        super(1, 3);
    }
}
