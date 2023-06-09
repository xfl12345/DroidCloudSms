package cc.xfl12345.android.droidcloudsms.model.database;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cc.xfl12345.android.droidcloudsms.model.ws.SmsTask;

public class SmsLog {

    private Long id;

    private Long utcTimeStamp;

    private String time;

    private String phoneNumber;

    private String validationCode;

    private String content;

    private Integer smsResultCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUtcTimeStamp() {
        return utcTimeStamp;
    }

    public void setUtcTimeStamp(Long utcTimeStamp) {
        this.utcTimeStamp = utcTimeStamp;
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

    public Integer getSmsResultCode() {
        return smsResultCode;
    }

    public void setSmsResultCode(Integer smsResultCode) {
        this.smsResultCode = smsResultCode;
    }

    public void setTime(ZonedDateTime zonedDateTime) {
        setTime(zonedDateTime.format(DateTimeFormatter.ISO_INSTANT));
        setUtcTimeStamp(zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

    public void setSmsTask(SmsTask smsTask) {
        setPhoneNumber(smsTask.getPhoneNumber());
        setValidationCode(smsTask.getValidationCode());
        setContent(smsTask.getSmsContent());
        setTime(ZonedDateTime.parse(smsTask.getCreateTime()));
    }
}
