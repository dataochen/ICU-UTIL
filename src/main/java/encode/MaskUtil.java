package encode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MaskUtil {

    private static Logger logger = LoggerFactory.getLogger(MaskUtil.class);
    private static final Set<String> cardNoKeys = new HashSet<>();
    private static final Set<String> cvvKeys = new HashSet<>();

    static {
        cardNoKeys.add("cardNo");
        cardNoKeys.add("merchantId");
        cardNoKeys.add("merchantNo");
        cardNoKeys.add("merchantNo4Decode");

        cvvKeys.add("securityCode");
        cvvKeys.add("cvv");
        cvvKeys.add("cvc");
        cvvKeys.add("cvv2");

    }

    enum Mode {
        CardInfo
    }


    public static String maskCardInfo(String json) {
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            jsonTraverse(jsonObject, Mode.CardInfo);
            return jsonObject.toJSONString();
        } catch (Exception e) {
            return json;
        }
    }

    private static void jsonTraverse(JSONObject jsonObject, Mode mode) {
        Iterator<String> iterator = jsonObject.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
//        for(String key : jsonObject.keySet()) {
            String value = jsonObject.getString(key);
            JSONObject childJsonObject = null;
            try {
                childJsonObject = JSON.parseObject(value);
            } catch (JSONException e) {
                if (Mode.CardInfo.equals(mode)) {
                    maskCard(jsonObject, key, iterator);
                }
            }
            if (childJsonObject != null) {
                jsonTraverse(childJsonObject, mode);
                jsonObject.put(key, childJsonObject.toJSONString());
            }
//        }
        }
    }

    private static void maskCard(JSONObject jsonObject, String key, Iterator<String> iterator) {
        String value = jsonObject.getString(key);
        if (cardNoKeys.contains(key) && StringUtils.isNotBlank(value)) {
            String maskedCardNo = maskCardNo(value);
            jsonObject.put(key, maskedCardNo);
        }
//            不打印cvv key和value
        if (cvvKeys.contains(key) && StringUtils.isNotBlank(value)) {
//            String maskedCvv = maskAll(value);
//            jsonObject.put(key, maskedCvv);
//             jsonObject.remove(key);
            iterator.remove();
        }
    }


    private static String maskCardNo(String cardNo) {
        if (cardNo == null) {
            return null;
        } else if (cardNo.length() > 10) {
            return cardNo.substring(0, 6) + "****" + cardNo.substring(cardNo.length() - 4);
        } else {
            return cardNo;
        }
    }

    private static String maskAll(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            mask.append("*");
        }
        return mask.toString();
    }

}
