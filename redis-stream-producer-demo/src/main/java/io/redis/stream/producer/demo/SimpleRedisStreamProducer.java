package io.redis.stream.producer.demo;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.HashMap;
import java.util.Map;

public class SimpleRedisStreamProducer {

    private String streamName;

    public SimpleRedisStreamProducer(String streamName) {
        this.streamName = streamName;
    }

    public void produce(int n, int sleepInterval) {
        RedisClient redisClient = RedisClient.create("redis://localhost:6379"); // change to reflect your environment
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> syncCommands = connection.sync();
        if(n == 0) {
            System.out.println( "Sending endless message in a loop with " + sleepInterval + " mili second(s) sleep");
            int i = 0;
            while(true) {
                send(syncCommands, i++, sleepInterval);
            }
        } else {
            System.out.println( String.format("\n Sending %s message(s)", n));

            for (int i = 0 ; i < n ; i++) {
                send(syncCommands, i, sleepInterval);
            }
        }

        System.out.println("\n");

        connection.close();
        redisClient.shutdown();
    }

    private void send(RedisCommands<String, String> syncCommands, int i, int sleepInterval) {
        Map<String, String> messageBody = createMessage(i);
        String messageId = syncCommands.xadd(
            streamName,
            messageBody);

        System.out.println(String.format("\tMessage %s : %s posted", messageId, messageBody));
        try {
            Thread.sleep(sleepInterval);
        } catch (InterruptedException e) {
            System.out.println("Caught exception in thread sleep");
        }
    }


    private Map<String, String> createMessage(int i) {
        Map<String, String> messageBody  = new HashMap<>();
        messageBody.put("speed", "15");
        messageBody.put("direction", "270");
        messageBody.put("sensor_ts", String.valueOf(System.currentTimeMillis()));
        messageBody.put("loop_info", String.valueOf( i ));
        return messageBody;
    }
}
