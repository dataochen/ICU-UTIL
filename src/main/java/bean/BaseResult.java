package bean;

/**
 * @author dataochen
 * @Description 统一返参实体
 * @date: 2019/7/9 15:29
 */
public class BaseResult {
    /**
     * 错误码
     */
    private String code;
    /**
     * 错误描述
     */
    private String desc;
    /**
     * 接口调用成功标志，如果是读接口代表读取成功，如果是写接口代表写入成功
     */
    private boolean success;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
