package jp.onehr.reflect.instance;

import jp.onehr.reflect.asserts.Assert;
import jp.onehr.reflect.map.WeakConcurrentMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 反射工具类
 */
public class FieldReflectUtils {

    /**
     * 字段缓存
     */
    private static final WeakConcurrentMap<Class<?>, Field[]> FIELDS_CACHE = new WeakConcurrentMap<>();

    /**
     * 查找指定类中是否包含指定名称对应的字段，包括所有字段（包括非public字段），也包括父类和Object类的字段
     *
     * @param beanClass 被查找字段的类,不能为null
     * @param name      字段名
     * @return 是否包含字段
     * @throws SecurityException 安全异常
     * @since 4.1.21
     */
    public static boolean hasField(Class<?> beanClass, String name) throws SecurityException {
        return null != getField(beanClass, name);
    }

    /**
     * 获取字段名，如果存在{@link Alias}注解，读取注解的值作为名称
     *
     * @param field 字段
     * @return 字段名
     * @since 5.1.6
     */
    public static String getFieldName(Field field) {
        if (null == field) {
            return null;
        }

        final Alias alias = field.getAnnotation(Alias.class);
        if (null != alias) {
            return alias.value();
        }

        return field.getName();
    }

    /**
     * 查找指定类中的指定name的字段（包括非public字段），也包括父类和Object类的字段， 字段不存在则返回{@code null}
     *
     * @param beanClass 被查找字段的类,不能为null
     * @param name      字段名
     * @return 字段
     * @throws SecurityException 安全异常
     */
    public static Field getField(Class<?> beanClass, String name) throws SecurityException {
        final Field[] fields = getFields(beanClass);
        return ArrayUtil.firstMatch((field) -> name.equals(getFieldName(field)), fields);
    }

    /**
     * 获取指定类中字段名和字段对应的有序Map，包括其父类中的字段<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass 类
     * @return 字段名和字段对应的Map，有序
     * @since 5.0.7
     */
    public static Map<String, Field> getFieldMap(Class<?> beanClass) {
        final Field[] fields = getFields(beanClass);
        final HashMap<String, Field> map = MapUtil.newHashMap(fields.length, true);
        for (Field field : fields) {
            map.put(field.getName(), field);
        }
        return map;
    }

    /**
     * 获得一个类中所有字段列表，包括其父类中的字段<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFields(Class<?> beanClass) throws SecurityException {
        Assert.notNull(beanClass);
        return FIELDS_CACHE.computeIfAbsent(beanClass, () -> getFieldsDirectly(beanClass, true));
    }


    /**
     * 获得一个类中所有满足条件的字段列表，包括其父类中的字段<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass   类
     * @param fieldFilter field过滤器，过滤掉不需要的field，{@code null}返回原集合
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     * @since 5.7.14
     */
    public static Field[] getFields(Class<?> beanClass, Filter<Field> fieldFilter) throws SecurityException {
        return ArrayUtil.filter(getFields(beanClass), fieldFilter);
    }

    /**
     * 获得一个类中所有字段列表，直接反射获取，无缓存<br>
     * 如果子类与父类中存在同名字段，则这两个字段同时存在，子类字段在前，父类字段在后。
     *
     * @param beanClass            类
     * @param withSuperClassFields 是否包括父类的字段列表
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFieldsDirectly(Class<?> beanClass, boolean withSuperClassFields) throws SecurityException {
        Assert.notNull(beanClass);

        Field[] allFields = null;
        Class<?> searchType = beanClass;
        Field[] declaredFields;
        while (searchType != null) {
            declaredFields = searchType.getDeclaredFields();
            if (null == allFields) {
                allFields = declaredFields;
            } else {
                allFields = ArrayUtil.append(allFields, declaredFields);
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }

        return allFields;
    }

    /**
     * 获取字段值
     *
     * @param obj       对象，如果static字段，此处为类
     * @param fieldName 字段名
     * @return 字段值
     * @throws UtilException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object obj, String fieldName) throws UtilException {
        if (null == obj || StrUtil.isBlank(fieldName)) {
            return null;
        }
        return getFieldValue(obj, getField(obj instanceof Class ? (Class<?>) obj : obj.getClass(), fieldName));
    }

    /**
     * 获取静态字段值
     *
     * @param field 字段
     * @return 字段值
     * @throws UtilException 包装IllegalAccessException异常
     * @since 5.1.0
     */
    public static Object getStaticFieldValue(Field field) throws UtilException {
        return getFieldValue(null, field);
    }

    /**
     * 获取字段值
     *
     * @param obj   对象，static字段则此字段为null
     * @param field 字段
     * @return 字段值
     * @throws UtilException 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object obj, Field field) throws UtilException {
        if (null == field) {
            return null;
        }
        if (obj instanceof Class) {
            // 静态字段获取时对象为null
            obj = null;
        }

        setAccessible(field);
        Object result;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            throw new UtilException(e, "IllegalAccess for {}.{}", field.getDeclaringClass(), field.getName());
        }
        return result;
    }

    /**
     * 获取所有字段的值
     *
     * @param obj bean对象，如果是static字段，此处为类class
     * @return 字段值数组
     * @since 4.1.17
     */
    public static Object[] getFieldsValue(Object obj) {
        return getFieldsValue(obj, null);
    }

    /**
     * 获取所有字段的值
     *
     * @param obj    bean对象，如果是static字段，此处为类class
     * @param filter 字段过滤器，，{@code null}返回原集合
     * @return 字段值数组
     * @since 5.8.23
     */
    public static Object[] getFieldsValue(Object obj, Filter<Field> filter) {
        if (null != obj) {
            final Field[] fields = getFields(obj instanceof Class ? (Class<?>) obj : obj.getClass(), filter);
            if (null != fields) {
                return ArrayUtil.map(fields, Object.class, field -> getFieldValue(obj, field));
            }
        }
        return null;
    }

    /**
     * 设置字段值<br>
     * 若值类型与字段类型不一致，则会尝试通过 {@link Convert} 进行转换<br>
     * 若字段类型是原始类型而传入的值是 null，则会将字段设置为对应原始类型的默认值（见 {@link ClassUtil#getDefaultValue(Class)}）
     * 如果是final字段，setFieldValue，调用这可以先调用 {@link ReflectUtil#removeFinalModify(Field)}方法去除final修饰符<br>
     *
     * @param obj       对象,static字段则此处传Class
     * @param fieldName 字段名
     * @param value     值，当值类型与字段类型不匹配时，会尝试转换
     * @throws UtilException 包装IllegalAccessException异常
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) throws UtilException {
        Assert.notNull(obj);
        Assert.notBlank(fieldName);

        final Field field = getField((obj instanceof Class) ? (Class<?>) obj : obj.getClass(), fieldName);
        Assert.notNull(field, "Field [{}] is not exist in [{}]", fieldName, obj.getClass().getName());
        setFieldValue(obj, field, value);
    }

    /**
     * 设置字段值<br>
     * 若值类型与字段类型不一致，则会尝试通过 {@link Convert} 进行转换<br>
     * 若字段类型是原始类型而传入的值是 null，则会将字段设置为对应原始类型的默认值（见 {@link ClassUtil#getDefaultValue(Class)}）<br>
     * 如果是final字段，setFieldValue，调用这可以先调用 {@link ReflectUtil#removeFinalModify(Field)}方法去除final修饰符
     *
     * @param obj   对象，如果是static字段，此参数为null
     * @param field 字段
     * @param value 值，当值类型与字段类型不匹配时，会尝试转换
     * @throws UtilException UtilException 包装IllegalAccessException异常
     */
    public static void setFieldValue(Object obj, Field field, Object value) throws UtilException {
        Assert.notNull(field, "Field in [{}] not exist !", obj);

        final Class<?> fieldType = field.getType();
        if (null != value) {
            if (false == fieldType.isAssignableFrom(value.getClass())) {
                //对于类型不同的字段，尝试转换，转换失败则使用原对象类型
                final Object targetValue = Convert.convert(fieldType, value);
                if (null != targetValue) {
                    value = targetValue;
                }
            }
        } else {
            // 获取null对应默认值，防止原始类型造成空指针问题
            value = ClassUtil.getDefaultValue(fieldType);
        }

        setAccessible(field);
        try {
            field.set(obj instanceof Class ? null : obj, value);
        } catch (IllegalAccessException e) {
            throw new UtilException(e, "IllegalAccess for {}.{}", obj, field.getName());
        }
    }

    /**
     * 是否为父类引用字段<br>
     * 当字段所在类是对象子类时（对象中定义的非static的class），会自动生成一个以"this$0"为名称的字段，指向父类对象
     *
     * @param field 字段
     * @return 是否为父类引用字段
     * @since 5.7.20
     */
    public static boolean isOuterClassField(Field field) {
        return "this$0".equals(field.getName());
    }

}
