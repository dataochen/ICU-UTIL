package encode;

import com.alibaba.fastjson.JSONObject;
import common.EncryptionUtil;
import org.apache.commons.lang3.RandomUtils;
import org.jose4j.base64url.Base64Url;
import org.jose4j.jwe.*;
import org.jose4j.jwx.Headers;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.*;
import java.util.Map;

public class JweUtils {
    private static final Logger log = LoggerFactory.getLogger(JweUtils.class);
    public String AES = "AES";
    @Value("${vts.jwe.channelSecurityContext.key}")
    private String SHARE_SECRET;
    @Value("${vts.jwe.kid.key}")
    private String KID_KEY;

    //这里是通过反射移除了isRestricted 的变量修饰符：final
    //然后将isRestricted 赋值为false即可
    static {
        //break JCE crypto policy limit
        try {
            final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
            final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
            final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");
            final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
            log.info("####################JWEUtils isRestrictedField befor update :{}", JSONObject.toJSONString(isRestrictedField));
            isRestrictedField.setAccessible(true);
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(isRestrictedField, isRestrictedField.getModifiers() & ~Modifier.FINAL);
            isRestrictedField.set(null, false);
            log.info("####################JWEUtils isRestrictedField after update:{}", JSONObject.toJSONString(isRestrictedField));
            Object o = isRestrictedField.get(null);
            log.debug("###JWEUtils modified field value={}", JSONObject.toJSONString(o));

            final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
            defaultPolicyField.setAccessible(true);
            final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

            final Field perms = cryptoPermissions.getDeclaredField("perms");
            perms.setAccessible(true);
            ((Map<?, ?>) perms.get(defaultPolicy)).clear();

            final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            defaultPolicy.add((Permission) instance.get(null));
            log.info("####################Successfully removed cryptography restrictions");
        } catch (Exception ex) {
            log.error("#######################JWEUtils break JCE crypto policy limit e={}", ex);
        }
    }

    /**
     * JWE加密
     *
     * @param plaintext json格式
     * @return
     * @throws JoseException
     */
    public String encryptionField(String plaintext) throws JoseException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(SHARE_SECRET.getBytes("UTF-8"));
            return encryptionField(plaintext, new AesKey(digest));
        } catch (NoSuchAlgorithmException e) {
            log.error("###encryptionField NoSuchAlgorithmException e={}", e);
        } catch (UnsupportedEncodingException e) {
            log.error("###encryptionField UnsupportedEncodingException e={}", e);
        }
        return "";
    }

    /**
     * @author:jirumutu
     * @date:2019/8/6
     * @param:[plaintext]
     * @return:java.lang.String
     * @description:JWE 加密
     */
    public String encryptionField(String plaintext, Key key) throws JoseException {
        JsonWebEncryption senderJwe = new JsonWebEncryption();
        /**********************header************************/
        /**
         * 设置Alg（algorithm)）
         */
        senderJwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A256GCMKW);

        /**
         * 参数kid
         */
        senderJwe.setKeyIdHeaderValue(KID_KEY);
        /**
         * 设置enc（encryption method）
         */
        senderJwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);
        senderJwe.setPayload(plaintext);

        /**
         * 自定义属性
         */
        senderJwe.setHeader("channelSecurityContext", "SHARED_SECRET");
        senderJwe.setHeader("typ", "JOSE");
//        senderJwe.setHeader("Kid", KID_KEY);
        senderJwe.setHeader("iat", String.valueOf(System.currentTimeMillis() / 1000));
//        超时时间60s
        senderJwe.setHeader("exp", String.valueOf((System.currentTimeMillis() / 1000 + 60)));
//        senderJwe.setHeader("iss", "12345678");
//        senderJwe.setHeader("aud", "128827728");

        /**********************body************************/
        /**
         * 明文
         */
//        senderJwe.setPlaintext(plaintext);
        /**
         * 设置IV（Initialization Vector）
         */
        senderJwe.setIv(Base64Utils.encodeUrlSafe(RandomUtils.nextBytes(96)));

        /**
         * 设置CEK 密钥
         */
        senderJwe.setKey(key);
        // 生成JWE压缩序列化，这是进行实际加密的地方。
        String compactSerialization = senderJwe.getCompactSerialization();
        log.info("compactSerialization:{}", compactSerialization);

//        decryptionField(compactSerialization, key);
        return compactSerialization;
    }


    /**
     * @author:jirumutu
     * @date:2019/8/6
     * @param:[ciphertext]
     * @return:java.lang.String
     * @description:JWE解密
     */
    public String decryptionField(String ciphertext, Key key) throws JoseException {
        JsonWebEncryption senderJwe = new JsonWebEncryption();

        /**
         * 设置Alg（algorithm)）
         */
        senderJwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A256GCMKW);
        /**
         * 设置enc（encryption method）
         */
        senderJwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_GCM);

        senderJwe.setKeyIdHeaderValue(KID_KEY);
        /**
         * 自定义属性
         */
//        senderJwe.setHeader("iv",Base64Utils.encodeToUrlSafeString(RandomUtils.nextBytes(96)));
//        senderJwe.setHeader("tag","");
//        senderJwe.setHeader("channelSecurityContext","kjlP{P7@{BUkK+4-W6z59UXePM#z#VR8ho#Yl/Cc");
//        senderJwe.setHeader("iat",String.valueOf(System.currentTimeMillis()));
//        senderJwe.setHeader("exp",String.valueOf(System.currentTimeMillis()+20000));
//        senderJwe.setHeader("iss","12345678");
//        senderJwe.setHeader("aud","128827728");
        /**
         * 设置IV（Initialization Vector）
         */
        senderJwe.setIv(Base64Utils.encodeUrlSafe(RandomUtils.nextBytes(96)));

        /**
         * 设置CEK 密钥
         */
        senderJwe.setKey(key);
        senderJwe.setCompactSerialization(ciphertext);
        String plaintextString = senderJwe.getPlaintextString();
        String keyIdHeaderValue = senderJwe.getKeyIdHeaderValue();
        System.out.println("===kid===" + keyIdHeaderValue);
        log.info("plaintextString:{}", senderJwe.getPlaintextString());
        return plaintextString;
    }


    /**
     * @author:jirumutu
     * @date:2019/9/9
     * @param:[plaintextText]
     * @return:java.lang.String
     * @description:JWE 加密  第二种方案
     */
    public String encryptionFieldOption(String plaintextText) throws JoseException {
        /**
         * 用AGCM256KW（alg）算法和cek-iv加密对cek
         */
        byte[] cek = RandomUtils.nextBytes(32);

        AesGcmContentEncryptionAlgorithm.Aes256Gcm aesGcmContentEncryptionAlg = new AesGcmContentEncryptionAlgorithm.Aes256Gcm();
        /**
         * Note：  The JWE Protected Header is input as the AAD (Additional Authenticated Data) parameter of the
         * authenticated encryption (AES-GCM) of the “text to encrypt”.
         */
        Headers headers = new Headers();
        headers.setStringHeaderValue("alg", "AGCM256KW");
//        headers.setStringHeaderValue("iv","SizeofIVistobe96bit");
//        headers.setStringHeaderValue("tag","128bitvalue");
//        headers.setStringHeaderValue("kid","50charAPIKey");
        headers.setStringHeaderValue("channelSecurityContext", "SHARED_SECRET");
        headers.setStringHeaderValue("enc", "AGCM256");
        headers.setStringHeaderValue("iat", "1429837145");
        headers.setStringHeaderValue("exp", "1429835524");
        headers.setStringHeaderValue("iss", "12345678");
        headers.setStringHeaderValue("aud", "128827728");
        String encodedHeader = headers.getEncodedHeader();
        //heade 解密
       /* String encodedHeader = "eyJhbGciOiJBR0NNMjU2S1ciLCJjaGFubmVsU2VjdXJpdHlDb250ZXh0IjoiU0hBUkVEX1NFQ1JFVCIsImVuYyI6IkFHQ00yNTYiLCJpYXQiOiIxNDI5ODM3MTQ1IiwiZXhwIjoiMTQyOTgzNTUyNCIsImlzcyI6IjEyMzQ1Njc4IiwiYXVkIjoiMTI4ODI3NzI4In0";
        headers.setFullHeaderAsJsonString(Base64Url.decodeToUtf8String(encodedHeader));*/

        byte[] aad = StringUtil.getBytesAscii(encodedHeader);
        byte[] iv = Base64Utils.encodeUrlSafe(RandomUtils.nextBytes(96));
        byte[] plaintext = StringUtil.getBytesUtf8(plaintextText);
        //tag
        ContentEncryptionParts encryptionParts = aesGcmContentEncryptionAlg.encrypt(plaintext, aad, cek, null, null);
        byte[] ciphertextByte = encryptionParts.getCiphertext();
        String ciphertextStr = Base64Url.encode(ciphertextByte);
        System.out.println("ciphertextStr = " + ciphertextStr);
    /*    System.out.println("encryptionParts.getCiphertext() = " + Base64Url.encode(encryptionParts.getCiphertext()));
        System.out.println("encryptionParts.getAuthenticationTag() = " + Base64Url.encode(encryptionParts.getAuthenticationTag()));
        System.out.println("encryptionParts.getIv() = " + Base64Url.encode(encryptionParts.getIv()));*/


        return ciphertextStr;
    }


    /**
     * @author:jirumutu
     * @date:2019/8/6
     * @param:[]
     * @return:void
     * @description:随机生成秘钥
     */
    public Key getKey(int length) {
        Key key = new AesKey(RandomUtils.nextBytes(length));
        log.info("key : {}", key.getEncoded().length);
        return key;
    }

    public Key getEncryptedKey(int length) {
        //c)	CEK的解释（随机生成256位的数字）
        //b)	用AGCM256KW（alg）算法和cek-iv加密对cek
        //a)	base64编码形式
        Key key = new AesKey(RandomUtils.nextBytes(length));
        return key;
    }

    public static void main(String[] args) {
        String s = "eyJhbGciOiJBMjU2R0NNS1ciLCJpdiI6InJBMmdJTDdlVHk0WWxpcEwiLCJ0YWciOiJaZXlhU3BvOVRoTFVnYnYtemdxc3p3IiwiZW5jIjoiQTI1NkdDTSIsInR5cCI6IkpPU0UiLCJraWQiOiJTVU1KT09MNDJWVFhWQVdOU1RGSzEzTl9FaHA1ZlpMSlhiV0pwaEdjaG1uRUQ2OXN3IiwiY2hhbm5lbFNlY3VyaXR5Q29udGV4dCI6IlNIQVJFRF9TRUNSRVQiLCJpYXQiOiIxNTc0MTQxNjM4In0.RAs77lSxlduKflq-ytn3SqLDnC8L0-83J4mur9wi-_I.utUSeyG6Dbo8qCqg.XMfcSB7V_59UXZ4eNLkhqwmzxShcH52QQfZX-7HgScV117VGrok7VWsbERCbgSkttP5WhUmA-nTkEw5QNDo8LMYn8fVbo2FHGiQZbfkSjADZi4lsBBLviBi8UK9pEiMc1Roxku7kgYKLwE3d2iivbJkVOkUaWXiqAj0FIJXYZoVOrsWlOXjIlcO2phbUwFKw3rM6-VOaTIVsE2n68glvYPxkGMPE4AZimPnBu0kBFcootS9bXktO09U7OIPlsf7tbjpG9m-BYahGAYyi-3pBiXNn9eUkMRh-KAktS4QgUJY9dIy9g_fwCorWhZt5ipscIRI7q3H-DpOTW_DJdzhq2FlXm0cFA_q0jTHHtNu5KWuJdnWPIA.XINCVTScqmwwcH8UkZXZWg";
        JweUtils jweUtils = new JweUtils();
        jweUtils.SHARE_SECRET = "kjlP{P7@{BUkK+4-W6z59UXePM#z#VR8ho#Yl/Cc";
        jweUtils.KID_KEY = "SUMJOOL42VTXVAWNSTFK13N_Ehp5fZLJXbWJphGchmnED69sw";
        String s1 = null;
        try {
            s1 = jweUtils.decryptionField(s, new AesKey(EncryptionUtil.getSHA256ForByte(jweUtils.SHARE_SECRET)));
        } catch (JoseException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(1);
    }
}
