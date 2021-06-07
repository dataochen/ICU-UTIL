package spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author dataochen
 * @Description 获取spring上下文
 * @date: 2020/8/4 10:10
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取指定bean
     * 例如：
     * ApplicationContextUtil.getObject("testBean")
     * @param name
     * @return
     */
    public static Object getObject(String name) {
        return applicationContext.getBean(name);
    }

    public static ApplicationContext getSpringContext() {
        return applicationContext;
    }
}
