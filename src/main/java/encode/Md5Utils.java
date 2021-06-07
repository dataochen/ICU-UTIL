package encode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * Md5加密方法
 *
 * @author Galax
 */
public class Md5Utils {
    private static final Logger log = LoggerFactory.getLogger(Md5Utils.class);
    private final static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z'};
    static String MD5 = "MD5";//加签方式：MD5

    private static byte[] md5(String s) {
        MessageDigest algorithm;
        try {
            algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(s.getBytes("UTF-8"));
            byte[] messageDigest = algorithm.digest();
            return messageDigest;
        } catch (Exception e) {
            log.error("MD5 Error...", e);
        }
        return null;
    }

    private static final String toHex(byte hash[]) {
        if (hash == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++) {
            if ((hash[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString(hash[i] & 0xff, 16));
        }
        return buf.toString();
    }

    public static String hash(String s) {
        try {
            return new String(toHex(md5(s)).getBytes("UTF-8"), "UTF-8");
        } catch (Exception e) {
            log.error("not supported charset...{}", e);
            return s;
        }
    }

    /**
     * 签名
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String sign(String data, String key) throws Exception {
        //得到明文的字节数组
        byte[] btInput = (data + key).getBytes();
        // 创建一个提供信息摘要算法的对象(MD5摘要算法)
        MessageDigest messageDigest = MessageDigest.getInstance(MD5);
        // 使用指定的字节更新摘要
        messageDigest.update(btInput);
        // 得到二进制的密文
        byte[] encryptData = messageDigest.digest();
        // 把密文转换成十六进制的字符串形式
        String encryptDataStr = bytesToHex(encryptData);
        return encryptDataStr;

    }

    /**
     * 使用指定密钥验证签名
     *
     * @param data
     * @param key
     * @param sign
     * @return
     * @throws Exception
     */
    public static boolean verifySign(String data, String key, String sign) throws Exception {
        //调用加签方法，看加签后的签名是否和接收到的一致
        String encryptData = sign(data, key);
        if (encryptData.equals(sign)) {
            return true;
        } else {
            return false;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        int k = 0;
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            byte byte0 = bytes[i];
            hexChars[k++] = hexDigits[byte0 >>> 4 & 0xf];
            hexChars[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(hexChars);
    }

}
