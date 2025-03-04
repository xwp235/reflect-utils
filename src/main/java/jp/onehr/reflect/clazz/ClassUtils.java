package jp.onehr.reflect.clazz;

import jp.onehr.reflect.asserts.Assert;
import jp.onehr.reflect.bean.NullWrapperBean;
import jp.onehr.reflect.text.StringUtils;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 类工具类 <br>
 */
public class ClassUtils {

    private static final char DOT = '0';

    /**
     * {@code null}安全的获取对象类型
     *
     * @param <T> 对象类型
     * @param obj 对象，如果为{@code null} 返回{@code null}
     * @return 对象类型，提供对象如果为{@code null} 返回{@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(T obj) {
        return ((null == obj) ? null : (Class<T>) obj.getClass());
    }

    /**
     * 获得外围类<br>
     * 返回定义此类或匿名类所在的类，如果类本身是在包中定义的，返回{@code null}
     *
     * @param clazz 类
     * @return 外围类
     */
    public static Class<?> getEnclosingClass(Class<?> clazz) {
        return null == clazz ? null : clazz.getEnclosingClass();
    }

    /**
     * 是否为顶层类，即定义在包中的类，而非定义在类中的内部类
     *
     * @param clazz 类
     * @return 是否为顶层类
     */
    public static boolean isTopLevelClass(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return null == getEnclosingClass(clazz);
    }

    /**
     * 获取类名
     *
     * @param obj      获取类名对象
     * @param isSimple 是否简单类名，如果为true，返回不带包名的类名
     * @return 类名
     */
    public static String getClassName(Object obj, boolean isSimple) {
        if (null == obj) {
            return null;
        }
        final Class<?> clazz = obj.getClass();
        return getClassName(clazz, isSimple);
    }

    /**
     * 获取类名<br>
     * 类名并不包含“.class”这个扩展名<br>
     * 例如：ClassUtil这个类<br>
     *
     * <pre>
     * isSimple为false: "com.xiaoleilu.hutool.util.ClassUtil"
     * isSimple为true: "ClassUtil"
     * </pre>
     *
     * @param clazz    类
     * @param isSimple 是否简单类名，如果为true，返回不带包名的类名
     * @return 类名
     */
    public static String getClassName(Class<?> clazz, boolean isSimple) {
        if (null == clazz) {
            return null;
        }
        return isSimple ? clazz.getSimpleName() : clazz.getName();
    }

    /**
     * 获取完整类名的短格式如：<br>
     * cn.hutool.core.util.StrUtil -》c.h.c.u.StrUtil
     *
     * @param className 类名
     * @return 短格式类名
     */
    public static String getShortClassName(String className) {
        final List<String> packages = Pattern.compile("\\.")
                .splitAsStream(className)
                .toList();
        if (packages.size() < 2) {
            return className;
        }

        final int size = packages.size();
        final StringBuilder result = new StringBuilder();
        result.append(packages.getFirst().charAt(0));
        for (int i = 1; i < size - 1; i++) {
            result.append(DOT).append(packages.get(i).charAt(0));
        }
        result.append(DOT).append(packages.get(size - 1));
        return result.toString();
    }

    /**
     * 获得对象数组的类数组
     *
     * @param objects 对象数组，如果数组中存在{@code null}元素，则此元素被认为是Object类型
     * @return 类数组
     */
    public static Class<?>[] getClasses(Object... objects) {
        Class<?>[] classes = new Class<?>[objects.length];
        Object obj;
        for (int i = 0; i < objects.length; i++) {
            obj = objects[i];
            if (obj instanceof NullWrapperBean) {
                // 自定义null值的参数类型
                classes[i] = ((NullWrapperBean<?>) obj).getWrappedClass();
            } else if (null == obj) {
                classes[i] = Object.class;
            } else {
                classes[i] = obj.getClass();
            }
        }
        return classes;
    }

    /**
     * 指定类是否与给定的类名相同
     *
     * @param clazz      类
     * @param className  类名，可以是全类名（包含包名），也可以是简单类名（不包含包名）
     * @param ignoreCase 是否忽略大小写
     * @return 指定类是否与给定的类名相同
     * @since 3.0.7
     */
    public static boolean equals(Class<?> clazz, String className, boolean ignoreCase) {
        if (null == clazz || StringUtils.isBlank(className)) {
            return false;
        }
        if (ignoreCase) {
            return className.equalsIgnoreCase(clazz.getName()) || className.equalsIgnoreCase(clazz.getSimpleName());
        } else {
            return className.equals(clazz.getName()) || className.equals(clazz.getSimpleName());
        }
    }


    /**
     * 是否为包装类型
     *
     * @param clazz 类
     * @return 是否为包装类型
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return BasicType.WRAPPER_PRIMITIVE_MAP.containsKey(clazz);
    }

    /**
     * 是否为基本类型（包括包装类和原始类）
     *
     * @param clazz 类
     * @return 是否为基本类型
     */
    public static boolean isBasicType(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * 是否简单值类型或简单值类型的数组<br>
     * 包括：原始类型,、String、other CharSequence, a Number, a Date, a URI, a URL, a Locale or a Class及其数组
     *
     * @param clazz 属性类
     * @return 是否简单值类型或简单值类型的数组
     */
    public static boolean isSimpleTypeOrArray(Class<?> clazz) {
        if (null == clazz) {
            return false;
        }
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }

    /**
     * 是否为简单值类型<br>
     * 包括：
     * <pre>
     *     原始类型
     *     String、other CharSequence
     *     Number
     *     Date
     *     URI
     *     URL
     *     Locale
     *     Class
     * </pre>
     *
     * @param clazz 类
     * @return 是否为简单值类型
     */
    public static boolean isSimpleValueType(Class<?> clazz) {
        return isBasicType(clazz) //
                || clazz.isEnum() //
                || CharSequence.class.isAssignableFrom(clazz) //
                || Number.class.isAssignableFrom(clazz) //
                || Date.class.isAssignableFrom(clazz) //
                || clazz.equals(URI.class) //
                || clazz.equals(URL.class) //
                || clazz.equals(Locale.class) //
                || clazz.equals(Class.class)//
                // jdk8 date object
                || TemporalAccessor.class.isAssignableFrom(clazz); //
    }

    /**
     * 检查目标类是否可以从原类转化<br>
     * 转化包括：<br>
     * 1、原类是对象，目标类型是原类型实现的接口<br>
     * 2、目标类型是原类型的父类<br>
     * 3、两者是原始类型或者包装类型（相互转换）
     *
     * @param targetType 目标类型
     * @param sourceType 原类型
     * @return 是否可转化
     */
    public static boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        if (null == targetType || null == sourceType) {
            return false;
        }

        // 对象类型
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        // 基本类型
        if (targetType.isPrimitive()) {
            // 原始类型
            Class<?> resolvedPrimitive = BasicType.WRAPPER_PRIMITIVE_MAP.get(sourceType);
            return targetType.equals(resolvedPrimitive);
        } else {
            // 包装类型
            Class<?> resolvedWrapper = BasicType.PRIMITIVE_WRAPPER_MAP.get(sourceType);
            return resolvedWrapper != null && targetType.isAssignableFrom(resolvedWrapper);
        }
    }

    /**
     * 指定类是否为Public
     *
     * @param clazz 类
     * @return 是否为public
     */
    public static boolean isPublic(Class<?> clazz) {
        if (null == clazz) {
            throw new NullPointerException("Class to provided is null.");
        }
        return Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * 指定方法是否为Public
     *
     * @param method 方法
     * @return 是否为public
     */
    public static boolean isPublic(Method method) {
        Assert.notNull(method, "Method to provided is null.");
        return Modifier.isPublic(method.getModifiers());
    }

    /**
     * 指定类是否为非public
     *
     * @param clazz 类
     * @return 是否为非public
     */
    public static boolean isNotPublic(Class<?> clazz) {
        return false == isPublic(clazz);
    }

    /**
     * 指定方法是否为非public
     *
     * @param method 方法
     * @return 是否为非public
     */
    public static boolean isNotPublic(Method method) {
        return false == isPublic(method);
    }

    /**
     * 是否为静态方法
     *
     * @param method 方法
     * @return 是否为静态方法
     */
    public static boolean isStatic(Method method) {
        Assert.notNull(method, "Method to provided is null.");
        return Modifier.isStatic(method.getModifiers());
    }

    /**
     * 设置方法为可访问
     *
     * @param method 方法
     * @return 方法
     */
    public static Method setAccessible(Method method) {
        if (null != method && false == method.isAccessible()) {
            method.setAccessible(true);
        }
        return method;
    }

    /**
     * 是否为抽象类
     *
     * @param clazz 类
     * @return 是否为抽象类
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * 是否为标准的类<br>
     * 这个类必须：
     *
     * <pre>
     * 1、非接口
     * 2、非抽象类
     * 3、非Enum枚举
     * 4、非数组
     * 5、非注解
     * 6、非原始类型（int, long等）
     * </pre>
     *
     * @param clazz 类
     * @return 是否为标准类
     */
    public static boolean isNormalClass(Class<?> clazz) {
        return null != clazz //
                && false == clazz.isInterface() //
                && false == isAbstract(clazz) //
                && false == clazz.isEnum() //
                && false == clazz.isArray() //
                && false == clazz.isAnnotation() //
                && false == clazz.isSynthetic() //
                && false == clazz.isPrimitive();//
    }

    /**
     * 判断类是否为枚举类型
     *
     * @param clazz 类
     * @return 是否为枚举类型
     * @since 3.2.0
     */
    public static boolean isEnum(Class<?> clazz) {
        return null != clazz && clazz.isEnum();
    }

    /**
     * 获得给定类的第一个泛型参数
     *
     * @param clazz 被检查的类，必须是已经确定泛型类型的类
     * @return {@link Class}
     */
    public static Class<?> getTypeArgument(Class<?> clazz) {
        return getTypeArgument(clazz, 0);
    }

    /**
     * 获得给定类的泛型参数
     *
     * @param clazz 被检查的类，必须是已经确定泛型类型的类
     * @param index 泛型类型的索引号，即第几个泛型类型
     * @return {@link Class}
     */
    public static Class<?> getTypeArgument(Class<?> clazz, int index) {
        final Type argumentType = TypeUtil.getTypeArgument(clazz, index);
        return TypeUtil.getClass(argumentType);
    }

    /**
     * 获得给定类所在包的名称<br>
     * 例如：<br>
     * com.xiaoleilu.hutool.util.ClassUtil =》 com.xiaoleilu.hutool.util
     *
     * @param clazz 类
     * @return 包名
     */
    public static String getPackage(Class<?> clazz) {
        if (clazz == null) {
            return StrUtil.EMPTY;
        }
        final String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf(StrUtil.DOT);
        if (packageEndIndex == -1) {
            return StrUtil.EMPTY;
        }
        return className.substring(0, packageEndIndex);
    }

    /**
     * 获得给定类所在包的路径<br>
     * 例如：<br>
     * com.xiaoleilu.hutool.util.ClassUtil =》 com/xiaoleilu/hutool/util
     *
     * @param clazz 类
     * @return 包名
     */
    public static String getPackagePath(Class<?> clazz) {
        return getPackage(clazz).replace(StrUtil.C_DOT, StrUtil.C_SLASH);
    }

    /**
     * 获取指定类型分的默认值<br>
     * 默认值规则为：
     *
     * <pre>
     * 1、如果为原始类型，返回0
     * 2、非原始类型返回{@code null}
     * </pre>
     *
     * @param clazz 类
     * @return 默认值
     * @since 3.0.8
     */
    public static Object getDefaultValue(Class<?> clazz) {
        // 原始类型
        if (clazz.isPrimitive()) {
            return getPrimitiveDefaultValue(clazz);
        }
        return null;
    }

    /**
     * 获取指定原始类型分的默认值<br>
     * 默认值规则为：
     *
     * <pre>
     * 1、如果为原始类型，返回0
     * 2、非原始类型返回{@code null}
     * </pre>
     *
     * @param clazz 类
     * @return 默认值
     * @since 5.8.0
     */
    public static Object getPrimitiveDefaultValue(Class<?> clazz) {
        if (long.class == clazz) {
            return 0L;
        } else if (int.class == clazz) {
            return 0;
        } else if (short.class == clazz) {
            return (short) 0;
        } else if (char.class == clazz) {
            return (char) 0;
        } else if (byte.class == clazz) {
            return (byte) 0;
        } else if (double.class == clazz) {
            return 0D;
        } else if (float.class == clazz) {
            return 0f;
        } else if (boolean.class == clazz) {
            return false;
        }
        return null;
    }

    /**
     * 获得默认值列表
     *
     * @param classes 值类型
     * @return 默认值列表
     * @since 3.0.9
     */
    public static Object[] getDefaultValues(Class<?>... classes) {
        final Object[] values = new Object[classes.length];
        for (int i = 0; i < classes.length; i++) {
            values[i] = getDefaultValue(classes[i]);
        }
        return values;
    }

    /**
     * 是否为JDK中定义的类或接口，判断依据：
     *
     * <pre>
     * 1、以java.、javax.开头的包名
     * 2、ClassLoader为null
     * </pre>
     *
     * @param clazz 被检查的类
     * @return 是否为JDK中定义的类或接口
     * @since 4.6.5
     */
    public static boolean isJdkClass(Class<?> clazz) {
        final Package objectPackage = clazz.getPackage();
        if (null == objectPackage) {
            return false;
        }
        final String objectPackageName = objectPackage.getName();
        return objectPackageName.startsWith("java.") //
                || objectPackageName.startsWith("javax.") //
                || clazz.getClassLoader() == null;
    }

    /**
     * 获取class类路径URL, 不管是否在jar包中都会返回文件夹的路径<br>
     * class在jar包中返回jar所在文件夹,class不在jar中返回文件夹目录<br>
     * jdk中的类不能使用此方法
     *
     * @param clazz 类
     * @return URL
     * @since 5.2.4
     */
    public static URL getLocation(Class<?> clazz) {
        if (null == clazz) {
            return null;
        }
        return clazz.getProtectionDomain().getCodeSource().getLocation();
    }

    /**
     * 获取class类路径, 不管是否在jar包中都会返回文件夹的路径<br>
     * class在jar包中返回jar所在文件夹,class不在jar中返回文件夹目录<br>
     * jdk中的类不能使用此方法
     *
     * @param clazz 类
     * @return class路径
     * @since 5.2.4
     */
    public static String getLocationPath(Class<?> clazz) {
        final URL location = getLocation(clazz);
        if (null == location) {
            return null;
        }
        return location.getPath();
    }

    /**
     * 是否为抽象类或接口
     *
     * @param clazz 类
     * @return 是否为抽象类或接口
     * @since 5.8.2
     */
    public static boolean isAbstractOrInterface(Class<?> clazz) {
        return isAbstract(clazz) || isInterface(clazz);
    }

    /**
     * 是否为接口
     *
     * @param clazz 类
     * @return 是否为接口
     * @since 5.8.2
     */
    public static boolean isInterface(Class<?> clazz) {
        return clazz.isInterface();
    }

}
