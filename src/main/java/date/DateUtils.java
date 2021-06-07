package date;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author chendatao
 * @since 1.0
 */
public class DateUtils {
    private static final String YMDHMS = "yyyy-MM-dd HH:mm:ss";

    public static String format(long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(YMDHMS);
        Date date = new Date();
        date.setTime(timestamp);
        return simpleDateFormat.format(date);
    }
    public static Date addDay(Date date, int day) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.add(Calendar.DAY_OF_MONTH, day);
        return instance.getTime();
    }

    public static String formatHm(Date date, TimeZone timeZone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(date);
    }

    /**
     * 根据小时和分钟获取时间
     *
     * @param hour
     * @param minute
     * @return
     */
    public static Date get4HourAndM(int hour, int minute) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.HOUR_OF_DAY, hour);
        instance.set(Calendar.MINUTE, minute);
        return instance.getTime();
    }
}
