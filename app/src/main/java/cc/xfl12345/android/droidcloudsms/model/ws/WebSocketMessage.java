package cc.xfl12345.android.droidcloudsms.model.ws;

public class WebSocketMessage {

    private Type messageType;

    private Object payload;

    public Type getMessageType() {
        return messageType;
    }

    public void setMessageType(Type messageType) {
        this.messageType = messageType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public enum Type {
        request,
        response
    }
}
