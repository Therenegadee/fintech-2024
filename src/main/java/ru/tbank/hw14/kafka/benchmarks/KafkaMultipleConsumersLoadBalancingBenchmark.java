package ru.tbank.hw14.kafka.benchmarks;

public class KafkaMultipleConsumersLoadBalancingBenchmark extends KafkaBaseBenchmark {
    public KafkaMultipleConsumersLoadBalancingBenchmark() {
        super(3, 3);
    }
}
