package date;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author dataochen
 * @Description 指定时区 date unix时间戳 json序列化
 * 时间戳 和date的转换
 * @date: 2020/12/8 12:14
 */
public class DateZoneTime extends SimpleDateFormat {
    private static final long serialVersionUID = 1445973574404038821L;
    private TimeZone timeZone;

    public DateZoneTime(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        super.format(date, toAppendTo, fieldPosition);
        long time = date.getTime();
//        系统默认时区
        int offset = TimeZone.getDefault().getOffset(time);
        int offset7 = timeZone.getOffset(time);
        long l = time - (offset - offset7);
        return new StringBuffer().append(l);
    }

    @Override
    public Date parse(String source) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        return parse(source, pos);
    }

    @Override
    public Date parse(String source, ParsePosition pos) {
        super.parse(source, pos);
        long time = System.currentTimeMillis();
        int offset = TimeZone.getDefault().getOffset(time);
        int offset7 = timeZone.getOffset(time);
        long l = Long.valueOf(source) + (offset - offset7);
        return new Date(l);
    }

}
