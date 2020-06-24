package io.redis.stream.consumer.demo;

import com.redis.demo.exception.MessagingException;

public class Main {
    public static void main(String[] args) throws MessagingException {
        System.out.println("======== Starting Consumer ========");
        if(args.length < 7) {
            System.out.println("USAGE: \n " +
                "arg1 -> count, \n" +
                "arg2 -> delete(true/false), \n" +
                "arg3 -> acks (true/false)\n" +
                "arg4 -> read pending message(true/false)\n" +
                "arg5 -> claim pending messages(true/false)\n" +
                "arg6 -> Prev Consumer Id for claim \n" +
                "arg7 -> AgentID");
            return;
        }
        System.out.println("Count or not " + args[0]);
        System.out.println("Delete or not " + args[1]);
        System.out.println("Acks or not " + args[2]);
        System.out.println("Read pending message " + args[3]);
        System.out.println("Claim pending message " + args[4]);
        System.out.println("Prev Consumer id for claiming pending messages " + args[5]);
        System.out.println("Agent ID " + args[6]);

        SimpleRedisStreamConsumer consumer = new SimpleRedisStreamConsumer(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        consumer.consume();
    }
}
