package cc.xfl12345.android.droidcloudsms.model;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class TimeUtils {

    public static String getNowTimeTextInISO8601() {
        return getNowTime().format(DateTimeFormatter.ISO_INSTANT);
    }

    public static ZonedDateTime getNowTime() {
        return ZonedDateTime.now(TimeZone.getDefault().toZoneId());
    }

    public static ZonedDateTime getTomorrowStartTimeInUTC(ZonedDateTime now) {
        return now
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .plusDays(1)
            .withZoneSameInstant(ZoneOffset.UTC);
    }

    public static ZonedDateTime getYesterdayStartTimeInUTC(ZonedDateTime now) {
        return now
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .minusDays(1)
            .withZoneSameInstant(ZoneOffset.UTC);
    }

    public static String parseISO8601Time2InChineseCustom(String iso8601Text) {
        return ZonedDateTime.parse(iso8601Text)
            .withZoneSameInstant(ZoneOffset.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

}
