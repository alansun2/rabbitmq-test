import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author AlanSun
 * @date 2020/3/25 10:50
 */
public class Producer {
    public final static String QUEUE_NAME = "test_queue";
    public final static String QUEUE_NAME1 = "test_queue1";
    public final static String ROUTE_KEY = "push";
    public final static String BIND_KEY = "push";
    public final static String EXCHANGE = "test-exchange";

    public static void main(String[] args) {
        Producer pushProducer = new Producer();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("111");
        factory.setPassword("123456");
        try (Connection connection = factory.newConnection(); final Channel channel = connection.createChannel()) {
//            System.out.println(connection.getAddress());
//            System.out.println(connection.getHeartbeat());
//            System.out.println(connection.getId());
//            System.out.println(connection.getPort());

            channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
                System.out.println("replyCode: " + replyCode);
                System.out.println("replyText: " + replyText);
                System.out.println(new String(body));
                System.out.println("------------------------");
            });
            channel.addConfirmListener(new ConfirmListener() {
                @Override
                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    System.out.println("handleAck");
                    System.out.println("multiple:" + multiple);
                    System.out.println("deliveryTag:" + deliveryTag);
                    System.out.println("------------------------");
                }

                @Override
                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    System.out.println("handleNack");
                    System.out.println("multiple:" + multiple);
                    System.out.println("deliveryTag:" + deliveryTag);
                }
            });
            channel.addShutdownListener(cause -> {
                System.out.println("shutdown");
                System.out.println("connection is open :" + connection.isOpen());
                cause.printStackTrace();
            });
            Map<String, Object> argsments = new HashMap<String, Object>();
//            argsments.put("x-message-ttl", 60000);// 队列内消息的过期时间，
            argsments.put("x-expires", 18000);// 队列在没有消费者时，自动删除的过期时间
            channel.queueDeclare(Producer.QUEUE_NAME, true, false, false, null);
            channel.exchangeDeclare(Producer.EXCHANGE, BuiltinExchangeType.TOPIC);
            channel.queueBind(Producer.QUEUE_NAME, Producer.EXCHANGE, Producer.BIND_KEY);
//            channel.queueBind(Producer.QUEUE_NAME1, Producer.EXCHANGE, Producer.BIND_KEY);

            String message = "Hello World";
            final AMQP.Confirm.SelectOk selectOk = channel.confirmSelect();
            for (int i = 0; i < 2; i++) {
                Thread.sleep(1000);
                message = message + ":" + i;
                final AMQP.BasicProperties build;
                if (i == 1) {
                    build = new AMQP.BasicProperties.Builder().expiration("30000").build();
                } else {
                    build = new AMQP.BasicProperties.Builder().expiration("10000").build();
                }

                channel.basicPublish(EXCHANGE, ROUTE_KEY, true, false, build, message.getBytes());
            }
            final boolean b = channel.waitForConfirms();
            System.out.println("wait confirm" + b);
//            final long nextPublishSeqNo = channel.getNextPublishSeqNo();
//            System.out.println(nextPublishSeqNo);
            System.out.println(" [x] Sent '" + message + "'");
            Thread.sleep(30000);
        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }

}
