package jp.onehr.reflect.clazz;

import jp.onehr.reflect.text.StringUtils;

import java.lang.reflect.Field;

public class ClassFieldUtils {

    /**
     * 查找指定类中的所有字段（包括非public字段）， 字段不存在则返回{@code null}
     *
     * @param clazz     被查找字段的类
     * @param fieldName 字段名
     * @return 字段
     * @throws SecurityException 安全异常
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) throws SecurityException {
        if (null == clazz || StringUtils.isBlank(fieldName)) {
            return null;
        }
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * 查找指定类中的所有字段（包括非public字段)
     *
     * @param clazz 被查找字段的类
     * @return 字段
     * @throws SecurityException 安全异常
     */
    public static Field[] getDeclaredFields(Class<?> clazz) throws SecurityException {
        if (null == clazz) {
            return null;
        }
        return clazz.getDeclaredFields();
    }

}
