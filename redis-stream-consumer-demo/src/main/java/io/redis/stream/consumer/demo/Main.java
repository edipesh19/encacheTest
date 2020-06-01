package io.redis.stream.consumer.demo;

public class Main {
    public static void main(String[] args) {
        System.out.println("======== Starting Consumer ========");
        if(args.length < 8) {
            System.out.println("USAGE: arg1 -> stream name, \n " +
                "arg2 -> Consumer group Id, \n" +
                "arg3 -> Consumer Id, \n" +
                "arg4 -> count, \n" +
                "arg5 -> delete(true/false), \n" +
                "arg6 -> acks (true/false)\n" +
                "arg7 -> read pending message(true/false)\n" +
                "arg8 -> claim pending messages(true/false)\n" +
                "arg9 -> Prev Consumer Id for claim");
            return;
        }
        System.out.println("Stream Name " + args[0]);
        System.out.println("Consumer group Id " + args[1]);
        System.out.println("Consumer Id " + args[2]);
        System.out.println("Count or not " + args[3]);
        System.out.println("Delete or not " + args[4]);
        System.out.println("Acks or not " + args[5]);
        System.out.println("Read pending message " + args[6]);
        System.out.println("Claim pending message " + args[7]);
        System.out.println("Prev Consumer id for claiming pending messages " + args[8]);

        SimpleRedisStreamConsumer consumer = new SimpleRedisStreamConsumer(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
        consumer.consume();
    }
}
