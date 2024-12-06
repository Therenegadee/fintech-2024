package ru.tbank.hw14.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMqConfiguration {

    private static final String QUEUE_NAME = "test-queue";
    private static final String RABBIT_HOST = "localhost";
    private static final int RABBIT_PORT = 5672;
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    public static class RabbitMqConsumer {
        private final Connection connection;
        private final Channel channel;

        public RabbitMqConsumer() throws IOException, TimeoutException {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBIT_HOST);
            factory.setPort(RABBIT_PORT);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        }

        public String consumeMessage() throws IOException {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            };

            return channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        }

        public void close() {
            try {
                if (channel != null) {
                    channel.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class RabbitMqProducer {
        private final int index;
        private final Connection connection;
        private final Channel channel;

        public RabbitMqProducer(Integer index) throws IOException, TimeoutException {
            this.index = index;
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBIT_HOST);
            factory.setPort(RABBIT_PORT);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        }

        public void sendMessage(String message) throws IOException {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int getIndex() {
            return index;
        }

        public void close() {
            try {
                if (channel != null) {
                    channel.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static class RabbitMqSendMessageConfirmationProducer {
        private final int index;
        private final Connection connection;
        private final Channel channel;

        public RabbitMqSendMessageConfirmationProducer(Integer index) throws IOException, TimeoutException {
            this.index = index;
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBIT_HOST);
            factory.setPort(RABBIT_PORT);
            factory.setUsername(USERNAME);
            factory.setPassword(PASSWORD);
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.confirmSelect();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        }

        public void sendMessage(String message) throws IOException, InterruptedException {
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            channel.waitForConfirms();
        }

        public int getIndex() {
            return index;
        }

        public void close() {
            try {
                if (channel != null) {
                    channel.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
