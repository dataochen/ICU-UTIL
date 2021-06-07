package common;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author dataochen
 * @Description 自动生成demo数据 工具类
 * @date: 2020/11/18 18:18
 */
public class MockDemoUtil {

    public static <T> void convert4Demo(T t) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Class<?> aClass = t.getClass();
        while (Object.class != aClass) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String firstChar = name.charAt(0) + "";
                String methodName = "set" + firstChar.toUpperCase() + name.substring(1);
                Method declaredMethod = null;
                try {
                    declaredMethod = aClass.getDeclaredMethod(methodName, field.getType());
                } catch (NoSuchMethodException e) {
                    continue;
                }
                Class<?> type = field.getType();
                try {
                    declaredMethod.invoke(t, demo4Type(field, type));
                } catch (NoSuchMethodException e) {
                    continue;
                }
            }
            aClass = aClass.getSuperclass();

        }
    }

    private static Object demo4Type(Field field, Class<?> type) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        if (type.getClassLoader() != null) {
//                    自定义类
            Object o = type.newInstance();
            convert4Demo(o);
            return o;
        }
        if (type == Boolean.class) {
            return true;
        } else if (type == Integer.class) {
            return 1;
        } else if (type == Long.class) {
            return 1L;
        } else if (type == Float.class) {
            return 1.0f;
        } else if (type == Double.class) {
            return 1.0d;
        } else if (type == String.class) {
            return "demoTest";

        } else if (type == BigDecimal.class) {
            return BigDecimal.TEN;

        } else if (type == Date.class) {
            return new Date();
        } else if (type == List.class) {
//            list不支持List<List>以及更多层
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {

                Object o = convertListOrMap((ParameterizedType) genericType);
                return o;
            }
            return Arrays.asList(new Object());
        } else if (type == Map.class) {
//            map ps:key 只支持基本类型和自定义类型 不支持List和Map；value不支持第2层以上又是List/Map的参数 比如List<List/Map> or Map<List/Map>
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                return convertListOrMap((ParameterizedType) genericType);
            } else {
                return new HashMap<>();
            }
        } else {

        }
        return null;
    }

    private static Object convertListOrMap(ParameterizedType parameterizedType) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Type rawType = parameterizedType.getRawType();
        if (rawType == List.class) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments[0] instanceof ParameterizedType) {
                Object o = convertListOrMap((ParameterizedType) actualTypeArguments[0]);
                return Collections.singletonList(o);
            } else {
                Object o = demo4Type(null, (Class<?>) actualTypeArguments[0]);
                return Collections.singletonList(o);
            }
        } else if (rawType == Map.class) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type key = actualTypeArguments[0];
            Type value = actualTypeArguments[1];
            Object keyObj = null, valueObj = null;
            if (key instanceof ParameterizedType) {
                keyObj = convertListOrMap((ParameterizedType) key);
            } else {
                keyObj = demo4Type(null, (Class<?>) key);
            }
            if (value instanceof ParameterizedType) {
                valueObj = convertListOrMap((ParameterizedType) value);
            } else {
                valueObj = demo4Type(null, (Class<?>) value);
            }
//            xxx 先用hashMap 后期补充其他Map的实现类
            HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
            objectObjectHashMap.put(keyObj, valueObj);
            return objectObjectHashMap;
        } else {
            return new HashMap<>();

        }
    }
}
