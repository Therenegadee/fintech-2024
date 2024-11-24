package ru.tbank.hw14.rabbitmq.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import ru.tbank.hw14.AbstractBenchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static ru.tbank.hw14.rabbitmq.RabbitMqConfiguration.RabbitMqConsumer;
import static ru.tbank.hw14.rabbitmq.RabbitMqConfiguration.RabbitMqProducer;

public abstract class RabbitMqBaseBenchmark extends AbstractBenchmark {

    private final int producersNumber;
    private final int consumersNumber;

    private List<RabbitMqProducer> producers = new ArrayList<>();
    private List<RabbitMqConsumer> consumers = new ArrayList<>();

    public RabbitMqBaseBenchmark(int producersNumber, int consumersNumber) {
        this.producersNumber = producersNumber;
        this.consumersNumber = consumersNumber;
    }

    @Setup(Level.Trial)
    public void setup() throws IOException, TimeoutException {
        for (int i = 0; i < producersNumber; i++) {
            producers.add(new RabbitMqProducer(i));
        }

        for (int i = 0; i < consumersNumber; i++) {
            consumers.add(new RabbitMqConsumer());
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        producers.forEach(RabbitMqProducer::close);
        consumers.forEach(RabbitMqConsumer::close);
    }

    @Benchmark
    public void benchmarkProduceConsumer(Blackhole blackhole) {
        producers.forEach(producer -> {
            try {
                var message = "тЕсТ сОоБщЕнИе оТ пРоДюСсЕрА с ИнДеКсОм: " + producer.getIndex();
                producer.sendMessage(message);
                blackhole.consume(message);
            } catch (IOException e) {
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
