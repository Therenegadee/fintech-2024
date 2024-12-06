package ru.tbank.hw14.kafka.benchmarks;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import ru.tbank.hw14.AbstractBenchmark;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaLoadBalancingProducerAllNodesAcksBenchmark extends AbstractBenchmark {

    private static final String TEST_TOPIC = "test-topic";
    private static final String BOOTSTRAP_ADDRESSES = "localhost:9092";

    private List<KafkaProducer<String, String>> producers = new ArrayList<>();
    private List<KafkaConsumer<String, String>> consumers = new ArrayList<>();

    @Setup(Level.Trial)
    public void setup() {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_ADDRESSES);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");

        int producersNumber = 3;

        for (int i = 0; i < producersNumber; i++) {
            producers.add(new KafkaProducer<>(producerConfig));
        }

        int consumersNumber = 3;
        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_ADDRESSES);
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        for (int i = 0; i < consumersNumber; i++) {
            var consumer = new KafkaConsumer<String, String>(consumerConfig);
            consumer.subscribe(List.of("test-topic"));
            consumers.add(consumer);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        producers.forEach(KafkaProducer::close);
        consumers.forEach(KafkaConsumer::close);
    }

    @Benchmark
    public void benchmarkProduceConsumer(Blackhole blackhole) {
        producers.forEach(producer -> {
            var sendFuture = producer.send(new ProducerRecord<>(TEST_TOPIC, "tEsT-kEy", "v4lu3"));
            blackhole.consume(sendFuture);
            blackhole.consume(producer);
        });

        consumers.forEach(consumer -> {
            var message = consumer.poll(Duration.ofMillis(100));
            blackhole.consume(message);
            blackhole.consume(consumer);
        });

        blackhole.consume(producers);
        blackhole.consume(consumers);
    }
}
