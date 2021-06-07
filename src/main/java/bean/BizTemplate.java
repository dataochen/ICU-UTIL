package bean;

/**
 * @author dataochen
 * @Description
 * @date: 2019/7/9 15:28
 */
public class BizTemplate {

    public void process(BaseResult baseResult, TemplateInterface templateInterface) {
        try {
            templateInterface.doCheck();
            templateInterface.doAction();
            baseResult.setCode("000000");
            baseResult.setDesc("success");
            baseResult.setSuccess(true);
        } catch (Exception e) {
            baseResult.setCode("999999");
            baseResult.setDesc(e.getMessage());
            baseResult.setSuccess(false);
        }
    }
}
