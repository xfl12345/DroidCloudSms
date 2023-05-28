package cc.xfl12345.android.droidcloudsms.model.http.response;

public class GenericJsonApiResponseData <T> extends BaseResponseObject {
    protected int code;
    protected T data;

    public void appendMessage(String msg) {
        if (getMessage() == null) {
            setMessage(msg);
        } else {
            if (getMessage().equals("")) {
                setMessage(msg);
            } else {
                setMessage(getMessage() + ";" + msg);
            }
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
