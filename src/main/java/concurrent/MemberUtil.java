package concurrent;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @author dataochen
 * @Description 使用TTL 构造线程上下文
 * 以语言为例
 * @date: 2020/11/26 14:39
 */
public class MemberUtil {

    private static TransmittableThreadLocal<String> threadLocal4Language = new TransmittableThreadLocal<>();


    public static String getLanguage() {
//降级语言 默认为TH
        String s = threadLocal4Language.get();
        if (null == s) {
            return "TH";
        }
        return s;
    }

    public static void setLanguage(String language) {
        threadLocal4Language.set(language);
    }

    public static void clearLanguage() {
        threadLocal4Language.remove();
    }

    public static void clear() {
        clearLanguage();
    }
}
