package spring;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializerBase;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 自定义BigDecimal 序列号方式
 * 1.无论是否指定JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN 都默认把BigDecimal转为toPlainString格式
 * 2.所有BigDecimal都转换为String
 * 单例获取
 *
 * @author chendatao
 */
public class BigDecimalAsStringSerializer
        extends ToStringSerializerBase {
    private BigDecimalAsStringSerializer() {
        super(BigDecimal.class);
    }

    private static BigDecimalAsStringSerializer instance;

    public static BigDecimalAsStringSerializer getInstance() {
        if (Objects.isNull(instance)) {
            synchronized (BigDecimalAsStringSerializer.class) {
                if (Objects.isNull(instance)) {
                    instance = new BigDecimalAsStringSerializer();
                }
            }
        }
        return instance;
    }

    /**
     * Copied from `jackson-core` class `GeneratorBase`
     */
    protected final static int MAX_BIG_DECIMAL_SCALE = 9999;

    private static final long serialVersionUID = 2950965738907453620L;


    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {
        // As per [databind#2513], should not delegate; also, never empty (numbers do
        // have "default value" to filter by, just not "empty"
        return false;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        final String text;
        final BigDecimal bd = (BigDecimal) value;
        // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector, so we need this
        if (!_verifyBigDecimalRange(gen, bd)) {
            // ... but wouldn't it be nice to trigger error via generator? Alas,
            // no method to do that. So we'll do...
            final String errorMsg = String.format(
                    "Attempt to write plain `java.math.BigDecimal` (see JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN) with illegal scale (%d): needs to be between [-%d, %d]",
                    bd.scale(), MAX_BIG_DECIMAL_SCALE, MAX_BIG_DECIMAL_SCALE);
            provider.reportMappingProblem(errorMsg);
        }
        text = bd.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        gen.writeString(text);
    }

    @Override
    public String valueToString(Object value) {
        // should never be called
        throw new IllegalStateException();
    }

    // 24-Aug-2016, tatu: [core#315] prevent possible DoS vector, so we need this
    protected boolean _verifyBigDecimalRange(JsonGenerator gen, BigDecimal value) throws IOException {
        int scale = value.scale();
        return ((scale >= -MAX_BIG_DECIMAL_SCALE) && (scale <= MAX_BIG_DECIMAL_SCALE));
    }
}