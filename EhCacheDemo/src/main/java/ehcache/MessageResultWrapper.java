package ehcache;

import com.oracle.dicom.agent.mediator.dto.AgentMessageResultDTO;

import java.util.Objects;
import java.util.Properties;

public class MessageResultWrapper {

    public MessageResultWrapper() {
        this(null);
    }

    public MessageResultWrapper(AgentMessageResultDTO msg) {
        this(msg, false);
    }

    public MessageResultWrapper(AgentMessageResultDTO msg, boolean markedForDelete) {
        this.message = msg;
        this.messageProperties = new Properties();
        this.messageProperties.setProperty(PROP_KEY_MSG_MARKED_FOR_DELETE, Boolean.FALSE.toString());
    }

    public MessageResultWrapper(AgentMessageResultDTO msg, Properties msgProps) {
        this.message = msg;
        this.messageProperties = msgProps;
    }

    public AgentMessageResultDTO getMessage() {
        return message;
    }

    public void setMessage(AgentMessageResultDTO message) {
        this.message = message;
    }

    public void markMessageForDelete() {
        setProperty(PROP_KEY_MSG_MARKED_FOR_DELETE, Boolean.TRUE.toString());
    }

    public String getProperty(String key) {
        return this.messageProperties.getProperty(key);
    }

    public Object setProperty(String key, String value) {
        return this.messageProperties.setProperty(key, value);
    }


    public Properties getMessageProperties() {
        return this.messageProperties;
    }

    public void setMessageProperties(Properties properties) {
        this.messageProperties = properties;
    }

    @Override
    public String toString() {
        StringBuilder strRepBldr = new StringBuilder("MessageResultWrapper={message={");
        if (this.message == null) {
            strRepBldr.append("null");
        } else {
            strRepBldr.append(this.message.toString());
        }
        strRepBldr.append("},messageProperties={").append(PROP_KEY_MSG_MARKED_FOR_DELETE)
            .append('=').append(this.getProperty(PROP_KEY_MSG_MARKED_FOR_DELETE)).append('}');

        return strRepBldr.toString();
    }

    private AgentMessageResultDTO message;
    private Properties messageProperties;

    public static final String PROP_KEY_MSG_MARKED_FOR_DELETE = "dicom.agent.messaging.message.markedForDelete";

    @Override
    public int hashCode() {
        return Objects.hash(message, messageProperties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageResultWrapper)) {
            return false;
        }
        MessageResultWrapper that = (MessageResultWrapper) o;
        return messageProperties.equals(that.messageProperties);
    }
}
