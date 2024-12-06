package ru.tbank.hw14.rabbitmq.benchmarks;

public class RabbitMultipleConsumersLoadBalancingBenchmark extends RabbitMqBaseBenchmark {
    public RabbitMultipleConsumersLoadBalancingBenchmark() {
        super(3, 3);
    }
}
