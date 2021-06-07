package common;

import file.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author dataochen @Description
 * @date: 2017/11/7 18:07
 */
public class PropertiesUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);
    private static final String DEFAULT_PATH = "/config.properties";
    private static Properties props = null;
    private static PropertiesUtil instance;


    private PropertiesUtil() {

    }

    /**
     * 获取指定key的value
     *
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        if (Objects.isNull(props)) {
            return null;
        }
        return props.getProperty(key);
    }

    /**
     * 获取实例 单例模式
     *
     * @param configPath
     * @return
     */
    public static PropertiesUtil getInstance(String configPath) {
        if (StringUtils.isEmpty(configPath)) {
            configPath = DEFAULT_PATH;
        }
        if (Objects.isNull(instance)) {
            synchronized (PropertiesUtil.class) {
                if (Objects.isNull(instance)) {
                    instance = new PropertiesUtil();
                    instance.init(configPath);
                }
            }
        }
        return instance;
    }

    private void init(String configPath) {
        Resource RESOURCE = new ClassPathResource(configPath);
        try {
            props = PropertiesLoaderUtils.loadProperties(RESOURCE);
        } catch (IOException e) {
            LOGGER.error("e={}", e);
        }

    }

}
