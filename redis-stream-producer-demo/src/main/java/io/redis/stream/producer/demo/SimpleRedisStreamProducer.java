package io.redis.stream.producer.demo;

import com.redis.demo.ConnectionFactoryImpl;
import com.redis.demo.RedisMessageClient;
import com.redis.demo.RedisMessageClientImpl;
import com.redis.demo.dto.AgentMessageDTO;
import com.redis.demo.dto.CallbackDTO;
import com.redis.demo.exception.MessagingException;

public class SimpleRedisStreamProducer {

    private RedisMessageClient redisMessageClient;
    private String consumerId;

    public SimpleRedisStreamProducer(String agentId) {
        redisMessageClient = new RedisMessageClientImpl(new ConnectionFactoryImpl());
        this.consumerId = agentId;
    }

    public void produce(int n, int sleepInterval) throws MessagingException {
        if (n == 0) {
            System.out.println("Sending endless message in a loop with " + sleepInterval + " mili second(s) sleep");
            int i = 0;
            while (true) {
                AgentMessageDTO agentMessageDTO = createMessage(i);
                redisMessageClient.produce("aggregator1", consumerId, agentMessageDTO);
                i++;
            }
        } else {
            System.out.println(String.format("\n Sending %s message(s)", n));
            AgentMessageDTO agentMessageDTO;
            for (int i = 0; i < n; i++) {
                agentMessageDTO = createMessage(i);
                redisMessageClient.produce("aggregator1", consumerId, agentMessageDTO);
            }
        }
    }

    private AgentMessageDTO createMessage(int i) {
        CallbackDTO callbackDTO = new CallbackDTO("XDGFHJ", CallbackDTO.Methods.POST, "JGHFGDFGH");
        AgentMessageDTO agentMessageDTO = new AgentMessageDTO("FDGHJ" + i, 2345, "HGHFGDFCGVH", "cvbhmn", "HGFGDFGHJK", callbackDTO, "fgh",
            "XGFCGHVB", "kgdgfhj");
        return agentMessageDTO;
    }
}
