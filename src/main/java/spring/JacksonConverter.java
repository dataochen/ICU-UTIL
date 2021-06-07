package spring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author chendatao
 * @since 1.0
 */
public class JacksonConverter {

    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // BigDecimal的json序列化推荐使用自定义序列化类 MoneySerializer
        objectMapper.setSerializerFactory(objectMapper.getSerializerFactory().withSerializerModifier
                (new Jackson4BigDecimal()));
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }

}
