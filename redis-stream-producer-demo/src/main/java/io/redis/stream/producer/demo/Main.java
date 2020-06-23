package io.redis.stream.producer.demo;

import com.redis.demo.exception.MessagingException;

public class Main {
    public static void main(String[] args) throws MessagingException {
        System.out.println("======== Starting Producer ========");
        if(args.length < 3) {
            System.out.println("USAGE:, arg1 -> number of record to produce pass zero for infinite, arg2 -> sleep interval after each record produced, arg3 -> agentId");
            return;
        }
        System.out.println("arg1 " + args[0]);
        System.out.println("arg2 " + args[1]);
        System.out.println("arg3 " + args[2]);
        SimpleRedisStreamProducer producer = new SimpleRedisStreamProducer(args[2]);
        producer.produce(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }
}
