package com.oracle.ehcache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

import java.nio.ByteBuffer;

public class KryoSerializer implements Serializer<MessageResultWrapper> {
    private static final Kryo kryo = new Kryo();

    public KryoSerializer(ClassLoader loader) {
    }


    @Override
    public ByteBuffer serialize(MessageResultWrapper object) throws SerializerException {
        Output output = new Output(4096);
        kryo.writeObject(output, object);
        return ByteBuffer.wrap(output.getBuffer());
    }

    @Override
    public MessageResultWrapper read(ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        Input input =  new Input(new ByteBufferInputStream(binary)) ;
        return kryo.readObject(input, MessageResultWrapper.class);
    }

    @Override
    public boolean equals(MessageResultWrapper object, ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        return object.equals(read(binary));
    }
}
