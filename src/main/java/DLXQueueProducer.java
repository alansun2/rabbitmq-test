import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

/**
 * @author AlanSun
 * @date 2020/3/25 10:50
 */
public class DLXQueueProducer {
    public final static String QUEUE_NAME = "dlx_queue";
    public final static String QUEUE_NAME1 = "dlx_queue1";
    public final static String ROUTE_KEY = "push";
    public final static String ROUTE_KEY1 = "some-routing-key";
    public final static String BIND_KEY = "push";
    public final static String EXCHANGE = "dlx-exchange";
    public final static String EXCHANGE1 = "dlx-exchange1";

    public static void main(String[] args) {
        DLXQueueProducer pushProducer = new DLXQueueProducer();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("ehooo");
        factory.setPassword("ehooo100");
        try (Connection connection = factory.newConnection(); final Channel channel = connection.createChannel()) {

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

            channel.exchangeDeclare(EXCHANGE1, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE1, BIND_KEY);

            // 死信队列
            Map<String, Object> argsments = new HashMap<>();
            argsments.put("x-dead-letter-exchange", EXCHANGE1);// 死信队列
            argsments.put("x-max-priority", 10);
//            argsments.put("x-dead-letter-routing-key", ROUTE_KEY1);// 指定 routing-key
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(QUEUE_NAME1, true, false, false, argsments);
            channel.queueBind(QUEUE_NAME1, EXCHANGE, BIND_KEY);

            Random random = new Random();
            final AMQP.Confirm.SelectOk selectOk = channel.confirmSelect();
            for (int i = 0; i < 10; i++) {
                final int i1 = random.nextInt(10);
                int ex = i1 * 1000;
                String message = "Hello World" + ":" + ex;

                final AMQP.BasicProperties build = new AMQP.BasicProperties.Builder().expiration(ex + "").deliveryMode(1).priority(ex).build();
                channel.basicPublish(EXCHANGE, ROUTE_KEY, true, false, build, message.getBytes());
                System.out.println(" [x] Sent '" + message + "'");
            }
            final boolean b = channel.waitForConfirms();
            System.out.println("wait confirm" + b);
//            final long nextPublishSeqNo = channel.getNextPublishSeqNo();
//            System.out.println(nextPublishSeqNo);

            Thread.sleep(5000);
        } catch (TimeoutException | IOException | InterruptedException e) {
            e.printStackTrace();
        }


    }

}
