import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author AlanSun
 * @date 2020/3/25 10:50
 */
public class PushProducer {
    private final static String QUEUE_NAME = "push_queue";
    private final static String ROUTE_KEY = "push.addedsdfsdf";
    private final static String EXCHANGE = "push_exchange";

    private static final List<String> pushTokens = Arrays.asList(
            "58b32ffbc8d87769dbe7c9a26bcbd77a56dd1a9fcf4d16a3ea23abbf62cd8cc8"
    );

    public static void main(String[] args) {
        PushProducer pushProducer = new PushProducer();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost:5672");
        factory.setUsername("ehooo");
        factory.setPassword("xxxx");

        try (Connection connection = factory.newConnection(); final Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            final PushMessage pushMessage = pushProducer.pushApple(6, 1, "test", null, pushTokens);
            final String message = JSON.toJSONString(pushMessage);
            channel.basicPublish(EXCHANGE, ROUTE_KEY, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }
    }

    public enum DeviceType {
        umeng, ios
    }

    /**
     * source，type，message，deviceType，deviceTokens
     * 以上必填
     *
     * @param source       推送到那个应用，具体请看 push-server 端的配置。必填
     * @param type         和前端配合的参数，定义跳转到那个页面。必填
     * @param timeToLive   消息存活时间，可选
     * @param message      消息。必填
     * @param iosSound     声音文件。可选
     * @param androidSound 声音文件。可选
     * @param deviceType   [umeng|ios]
     * @param deviceTokens pushToken
     * @param extras       额外参数。可选
     */
    public PushMessage push(Integer source, Integer type, Integer timeToLive, String message, String iosSound, String androidSound, DeviceType deviceType, List<String> deviceTokens, Map<String, Object> extras) {
        PushMessage pushMessage = new PushMessage();

        if (deviceType == DeviceType.ios) {
            // ios push setup
            IosPayload iosPayload = new IosPayload();
            iosPayload.setAlert(message);
            iosPayload.setBadge(1);
            if (!StringUtils.isEmpty(iosSound)) {
                iosPayload.setSound(iosSound);
            }
            pushMessage.setIosPayload(iosPayload);
        } else {
            // 安卓 push setup
            UmengPayload umengPayload = new UmengPayload();
            if (timeToLive != null) {
                umengPayload.setTimeToLive(timeToLive);
            }
            if (!StringUtils.isEmpty(androidSound)) {
                umengPayload.setSound(androidSound);
            }
            umengPayload.setText(message);
            pushMessage.setUmengPayload(umengPayload);
        }

        if (extras != null) {
            pushMessage.setExtras(extras);
        }
        pushMessage.setSource(source);
        pushMessage.setExtras("type", type);
        pushMessage.setDeviceType(deviceType.toString());
        pushMessage.setDeviceTokens(deviceTokens);
        return pushMessage;
    }

    /**
     * 有声
     */
    public PushMessage pushUmeng(Integer source, Integer type, String message, String androidSound, List<String> deviceTokens) {
        return this.push(source, type, null, message, null, androidSound, DeviceType.umeng, deviceTokens, null);
    }

    /**
     * 有声
     */
    public PushMessage pushUmengSingle(Integer source, Integer type, String message, String androidSound, String deviceTokens) {
        return this.push(source, type, null, message, null, androidSound, DeviceType.umeng, Collections.singletonList(deviceTokens), null);
    }

    /**
     * 有声
     */
    public PushMessage pushApple(Integer source, Integer type, String message, String iosSound, List<String> deviceTokens) {
        return this.push(source, type, null, message, iosSound, null, DeviceType.ios, deviceTokens, null);
    }

    /**
     * 有声
     */
    public PushMessage pushAppleSingle(Integer source, Integer type, String message, String iosSound, String deviceTokens) {
        return this.push(source, type, null, message, iosSound, null, DeviceType.ios, Collections.singletonList(deviceTokens), null);
    }

    @Getter
    @Setter
    @ToString
    static class PushMessage implements Serializable {
        /**
         * 设备tokens
         */
        protected List<String> deviceTokens;

        protected List<Integer> userIds;

        protected Integer smiid;

        protected String deviceType;

        protected UmengPayload umengPayload;

        protected IosPayload iosPayload;

        protected Integer source;

        protected Map<String, Object> extras;

        protected Object transferedMsg;

        public PushMessage setExtras(String k, Object v) {
            if (extras == null) {
                extras = new HashMap<>();
            }
            this.extras.put(k, v);
            return this;
        }
    }

    @Data
    static class UmengPayload {
        private String ticker;

        private String title;

        private String text;

        private String timestamp;

        private String type;

        private String alias;

        private String description;
        //消息存活时间，默认1500秒
        private int timeToLive = 1500;

        private String sound;

        private Map<String, String> payload;
    }

    @Data
    static class IosPayload {
        private String alert;

        private int badge;

        private String sound;
    }

}
