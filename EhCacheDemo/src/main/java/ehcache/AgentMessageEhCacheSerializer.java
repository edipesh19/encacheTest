package ehcache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ehcache.spi.serialization.Serializer;
import org.ehcache.spi.serialization.SerializerException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class AgentMessageEhCacheSerializer implements Serializer<MessageResultWrapper> {

    private String encoding = "UTF-8";
    private static ObjectMapper c_objMapper;

    public AgentMessageEhCacheSerializer() {
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public AgentMessageEhCacheSerializer(ClassLoader loader) {
        c_objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ByteBuffer serialize(MessageResultWrapper object) throws SerializerException {
        try {
            if (object == null) {
                return null;
            } else {
                ByteBuffer b =  ByteBuffer.wrap(c_objMapper.writeValueAsString(object).getBytes(this.encoding));
                return b;
            }
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            String errMsg = "Error when serializing MessageResultWrapper to byte[]";
            System.out.println(errMsg);
            return null;
        }
    }

    @Override
    public MessageResultWrapper read(ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        try {
            if (binary == null) {
                return null;
            } else {
                //String temp = new String(binary.array(), this.encoding);
                //return c_objMapper.readValue(temp, MessageResultWrapper.class);
                String s;
                if (binary.hasArray()) {
                    s = new String(binary.array(),
                        binary.arrayOffset() + binary.position(),
                        binary.remaining(), this.encoding);
                } else {
                    final byte[] b = new byte[binary.remaining()];
                    binary.duplicate().get(b);
                    s = new String(b);
                }
                return c_objMapper.readValue(s, MessageResultWrapper.class);
            }
        } catch (IOException e) {
            String errMsg = "Error when reading MessageResultWrapper to byte[]";
            e.printStackTrace();
            System.out.println(errMsg);
            return null;
        }
    }

    @Override
    public boolean equals(MessageResultWrapper object, ByteBuffer binary) throws ClassNotFoundException, SerializerException {
        return object.equals(read(binary));
    }
}
