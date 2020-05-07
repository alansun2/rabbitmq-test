import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author AlanSun
 * @date 2020/3/25 10:51
 */
public class Consumer1 {
    public final static String BIND_KEY = "push.*";

    public static void main(String[] args) throws IOException, TimeoutException {
        Producer pushProducer = new Producer();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("123456");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(Producer.QUEUE_NAME, true, false, true, null);
        channel.exchangeDeclare(Producer.EXCHANGE, BuiltinExchangeType.TOPIC);
        channel.queueBind(Producer.QUEUE_NAME, Producer.EXCHANGE, BIND_KEY);
        channel.basicQos(10);

        AtomicInteger i = new AtomicInteger();
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
            System.out.println(i.get() + " [x] Received '" + message + "'");
            i.incrementAndGet();
//            if (i.longValue() >= 10) {
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
//                i.set(0);
//            }
        };
        channel.basicConsume(Producer.QUEUE_NAME, false, deliverCallback, consumerTag -> {
            System.out.println("cancel: " + consumerTag);
        });
    }
}
