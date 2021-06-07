package bean;

import java.util.List;

/**
 * @author dataochen
 * @Description 通用集合实体封装类
 * @date: 2019/7/9 15:29
 */
public class BaseListResult<T> extends BaseResult {
    /**
     * 集合实体
     */
    private List<T> data;
}
