package spring;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author dataochen
 * @Description
 * @date: 2020/12/18 10:39
 */
public class Jackson4BigDecimal extends BeanSerializerModifier {
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            final JavaType javaType = writer.getType();
            final Class<?> rawClass = javaType.getRawClass();
            if (BigDecimal.class.equals(rawClass)) {
//                使用NumberSerializer.bigDecimalAsStringSerializer()来实现BigDecimal类型序列化为String
                JsonSerializer<?> jsonSerializer = BigDecimalAsStringSerializer.getInstance();
                JsonSerializer<Object> jsonSerializer1 = (JsonSerializer<Object>) jsonSerializer;
                writer.assignSerializer(jsonSerializer1);
            }
        }
        return beanProperties;
    }
}
