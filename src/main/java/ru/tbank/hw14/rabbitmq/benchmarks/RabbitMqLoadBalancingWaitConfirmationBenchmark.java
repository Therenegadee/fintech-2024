package ru.tbank.hw14.rabbitmq.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import ru.tbank.hw14.AbstractBenchmark;
import ru.tbank.hw14.rabbitmq.RabbitMqConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;


public abstract class RabbitMqLoadBalancingWaitConfirmationBenchmark extends AbstractBenchmark {

    private List<RabbitMqConfiguration.RabbitMqSendMessageConfirmationProducer> producers = new ArrayList<>();
    private List<RabbitMqConfiguration.RabbitMqConsumer> consumers = new ArrayList<>();

    @Setup(Level.Trial)
    public void setup() throws IOException, TimeoutException {
        for (int i = 0; i < 3; i++) {
            producers.add(new RabbitMqConfiguration.RabbitMqSendMessageConfirmationProducer(i));
        }

        for (int i = 0; i < 3; i++) {
            consumers.add(new RabbitMqConfiguration.RabbitMqConsumer());
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        producers.forEach(RabbitMqConfiguration.RabbitMqSendMessageConfirmationProducer::close);
        consumers.forEach(RabbitMqConfiguration.RabbitMqConsumer::close);
    }

    @Benchmark
    public void benchmarkProduceConsumer(Blackhole blackhole) {
        producers.forEach(producer -> {
            try {
                var message = "тЕсТ сОоБщЕнИе оТ пРоДюСсЕрА с ИнДеКсОм: " + producer.getIndex();
                producer.sendMessage(message);
                blackhole.consume(message);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            blackhole.consume(producer);
        });
        consumers.forEach(consumer -> {
            try {
                var message = consumer.consumeMessage();
                blackhole.consume(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            blackhole.consume(consumer);
        });
        blackhole.consume(producers);
        blackhole.consume(consumers);
    }
}
