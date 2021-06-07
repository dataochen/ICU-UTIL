package format;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author dataochen
 * @Description
 * @date: 2020/11/13 18:27
 */
public class gsonUtils {
    private static final Gson gson = new Gson();
    private static final Gson gsonTimeUnix = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DateSerializer())
            .registerTypeAdapter(Date.class, new DateDeserializer())
            .setDateFormat(DateFormat.LONG).create();

    public static String toJsonString(Object o) {
        return gson.toJson(o);
    }


    public static <T> T parseObject(String s, Class<T> clazz) {
        return gson.fromJson(s, clazz);
    }

    /**
     * 增强版
     * 可对date类型的属性转换为unix时间戳 而不是默认的日期格式Dec 14, 2020 5:29:00 PM
     *
     * @param o
     * @param flag
     * @return
     */
    public static String toJsonString(Object o, boolean flag) {
        return flag ? gsonTimeUnix.toJson(o) : toJsonString(o);
    }

    public static <T> T parseObject(String s, Class<T> clazz, boolean flag) {
        return flag ? gsonTimeUnix.fromJson(s, clazz) : parseObject(s, clazz);
    }

    /**
     * date和long的转换
     */
    static class DateSerializer implements JsonSerializer<Date> {

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getTime());
        }
    }

    static class DateDeserializer implements JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(json.getAsJsonPrimitive().getAsLong());
        }
    }

}
