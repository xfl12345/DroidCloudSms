package cc.xfl12345.android.droidcloudsms.model.database;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationLog {
    private Long id;

    private Long utcTimeStamp;

    private String time;

    private String tag;

    private String content;

    private Integer logLevel;


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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }

    public void setTime(ZonedDateTime zonedDateTime) {
        setTime(zonedDateTime.format(DateTimeFormatter.ISO_INSTANT));
        setUtcTimeStamp(zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

}
