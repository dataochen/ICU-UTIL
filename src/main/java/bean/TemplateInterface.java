package bean;

/**
 * @author dataochen
 * @Description 模板
 * @date: 2019/7/9 15:31
 */
public interface TemplateInterface {
    /**
     * 检查参数
     */
    public void doCheck();

    /**
     * 处理事务
     */
    public void doAction();
}
