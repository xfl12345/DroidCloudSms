package cc.xfl12345.android.droidcloudsms.model.database;

import cc.xfl12345.android.droidcloudsms.model.ws.SmsTask;

public class SmsLog {

    private Long id;

    private String time;

    private String phoneNumber;

    private String validationCode;

    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSmsTask(SmsTask smsTask) {
        setPhoneNumber(smsTask.getPhoneNumber());
        setValidationCode(smsTask.getValidationCode());
        // SQLiteDatabase
        setContent(smsTask.getSmsContent());
        setTime(smsTask.getCreateTime());
    }
}
