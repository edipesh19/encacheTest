package io.redis.stream.consumer.demo;

public class Main {
    public static void main(String[] args) {
        System.out.println("======== Starting Consumer ========");
        if(args.length < 3) {
            System.out.println("USAGE: arg1 -> stream name, arg2 -> Consumer group Id, arg3 -> Consumer Id" );
            return;
        }
        System.out.println("Stream Name " + args[0]);
        System.out.println("Consumer group Id " + args[1]);
        System.out.println("Consumer Id " + args[2]);

        SimpleRedisStreamConsumer consumer = new SimpleRedisStreamConsumer(args[0], args[1], args[2]);
        consumer.consume();
    }
}
