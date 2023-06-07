package cc.xfl12345.android.droidcloudsms.model;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class TimeUtils {

    public static String getNowTimeInISO8601() {
        return ZonedDateTime.now(TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ISO_INSTANT);
    }

}
