package jp.onehr.reflect.instance;

import jp.onehr.reflect.asserts.Assert;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

public class NewInstanceReflectUtils {

    /**
     * 实例化对象
     *
     * @param <T>   对象类型
     * @param clazz 类名
     * @return 对象
     * @throws UtilException 包装各类异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String clazz) throws UtilException {
        try {
            return (T) Class.forName(clazz).newInstance();
        } catch (Exception e) {
            throw new UtilException(e, "Instance class [{}] error!", clazz);
        }
    }

    /**
     * 实例化对象
     *
     * @param <T>    对象类型
     * @param clazz  类
     * @param params 构造函数参数
     * @return 对象
     * @throws UtilException 包装各类异常
     */
    public static <T> T newInstance(Class<T> clazz, Object... params) throws UtilException {
        if (ArrayUtil.isEmpty(params)) {
            final Constructor<T> constructor = getConstructor(clazz);
            if (null == constructor) {
                throw new UtilException("No constructor for [{}]", clazz);
            }
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new UtilException(e, "Instance class [{}] error!", clazz);
            }
        }

        final Class<?>[] paramTypes = ClassUtil.getClasses(params);
        final Constructor<T> constructor = getConstructor(clazz, paramTypes);
        if (null == constructor) {
            throw new UtilException("No Constructor matched for parameter types: [{}]", new Object[]{paramTypes});
        }
        try {
            return constructor.newInstance(params);
        } catch (Exception e) {
            throw new UtilException(e, "Instance class [{}] error!", clazz);
        }
    }

    /**
     * 尝试遍历并调用此类的所有构造方法，直到构造成功并返回
     * <p>
     * 对于某些特殊的接口，按照其默认实现实例化，例如：
     * <pre>
     *     Map       -》 HashMap
     *     Collction -》 ArrayList
     *     List      -》 ArrayList
     *     Set       -》 HashSet
     * </pre>
     *
     * @param <T>  对象类型
     * @param type 被构造的类
     * @return 构造后的对象，构造失败返回{@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstanceIfPossible(Class<T> type) {
        Assert.notNull(type);

        // 原始类型
        if (type.isPrimitive()) {
            return (T) ClassUtil.getPrimitiveDefaultValue(type);
        }

        // 某些特殊接口的实例化按照默认实现进行
        if (type.isAssignableFrom(AbstractMap.class)) {
            type = (Class<T>) HashMap.class;
        } else if (type.isAssignableFrom(List.class)) {
            type = (Class<T>) ArrayList.class;
        } else if (type.isAssignableFrom(Set.class)) {
            type = (Class<T>) HashSet.class;
        }

        try {
            return newInstance(type);
        } catch (Exception e) {
            // ignore
            // 默认构造不存在的情况下查找其它构造
        }

        // 枚举
        if (type.isEnum()) {
            return type.getEnumConstants()[0];
        }

        // 数组
        if (type.isArray()) {
            return (T) Array.newInstance(type.getComponentType(), 0);
        }

        final Constructor<T>[] constructors = getConstructors(type);
        Class<?>[] parameterTypes;
        for (Constructor<T> constructor : constructors) {
            parameterTypes = constructor.getParameterTypes();
            if (0 == parameterTypes.length) {
                continue;
            }
            setAccessible(constructor);
            try {
                return constructor.newInstance(ClassUtil.getDefaultValues(parameterTypes));
            } catch (Exception ignore) {
                // 构造出错时继续尝试下一种构造方式
            }
        }
        return null;
    }
    
}
