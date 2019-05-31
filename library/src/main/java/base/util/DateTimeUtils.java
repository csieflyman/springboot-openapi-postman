package base.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author csieflyman
 */
public class DateTimeUtils {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String UTC_ZONE = "UTC";
    public static final ZoneId UTC_ZONE_ID = ZoneId.of(UTC_ZONE);
    public static final DateTimeFormatter UTC_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(UTC_ZONE_ID);
    public static final DateTimeFormatter UTC_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).withZone(UTC_ZONE_ID);

    private DateTimeUtils() {

    }
}
