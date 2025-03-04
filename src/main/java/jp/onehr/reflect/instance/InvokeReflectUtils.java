package jp.onehr.reflect.instance;

import jp.onehr.reflect.asserts.Assert;
import jp.onehr.reflect.bean.NullWrapperBean;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class InvokeReflectUtils {

    /**
     * 执行静态方法
     *
     * @param <T>    对象类型
     * @param method 方法（对象方法或static方法都可）
     * @param args   参数对象
     * @return 结果
     * @throws UtilException 多种异常包装
     */
    public static <T> T invokeStatic(Method method, Object... args) throws UtilException {
        return invoke(null, method, args);
    }

    /**
     * 执行方法<br>
     * 执行前要检查给定参数：
     *
     * <pre>
     * 1. 参数个数是否与方法参数个数一致
     * 2. 如果某个参数为null但是方法这个位置的参数为原始类型，则赋予原始类型默认值
     * </pre>
     *
     * @param <T>    返回对象类型
     * @param obj    对象，如果执行静态方法，此值为{@code null}
     * @param method 方法（对象方法或static方法都可）
     * @param args   参数对象
     * @return 结果
     * @throws UtilException 一些列异常的包装
     */
    public static <T> T invokeWithCheck(Object obj, Method method, Object... args) throws UtilException {
        final Class<?>[] types = method.getParameterTypes();
        if (null != args) {
            Assert.isTrue(args.length == types.length, "Params length [{}] is not fit for param length [{}] of method !", args.length, types.length);
            Class<?> type;
            for (int i = 0; i < args.length; i++) {
                type = types[i];
                if (type.isPrimitive() && null == args[i]) {
                    // 参数是原始类型，而传入参数为null时赋予默认值
                    args[i] = ClassUtil.getDefaultValue(type);
                }
            }
        }

        return invoke(obj, method, args);
    }

    /**
     * 执行方法
     *
     * <p>
     * 对于用户传入参数会做必要检查，包括：
     *
     * <pre>
     *     1、忽略多余的参数
     *     2、参数不够补齐默认值
     *     3、传入参数为null，但是目标参数类型为原始类型，做转换
     * </pre>
     *
     * @param <T>    返回对象类型
     * @param obj    对象，如果执行静态方法，此值为{@code null}
     * @param method 方法（对象方法或static方法都可）
     * @param args   参数对象
     * @return 结果
     * @throws InvocationTargetRuntimeException 目标方法执行异常
     * @throws UtilException                    {@link IllegalAccessException}异常的包装
     */
    public static <T> T invoke(Object obj, Method method, Object... args) throws InvocationTargetRuntimeException, UtilException {
        try {
            return invokeRaw(obj, method, args);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new UtilException(e);
        }
    }

    /**
     * 执行方法
     *
     * <p>
     * 对于用户传入参数会做必要检查，包括：
     *
     * <pre>
     *     1、忽略多余的参数
     *     2、参数不够补齐默认值
     *     3、传入参数为null，但是目标参数类型为原始类型，做转换
     * </pre>
     *
     * @param <T>    返回对象类型
     * @param obj    对象，如果执行静态方法，此值为{@code null}
     * @param method 方法（对象方法或static方法都可）
     * @param args   参数对象
     * @return 结果
     * @throws InvocationTargetException 目标方法执行异常
     * @throws IllegalAccessException    访问异常
     * @since 5.8.1
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeRaw(Object obj, Method method, Object... args) throws InvocationTargetException, IllegalAccessException {
        setAccessible(method);

        // 检查用户传入参数：
        // 1、忽略多余的参数
        // 2、参数不够补齐默认值
        // 3、通过NullWrapperBean传递的参数,会直接赋值null
        // 4、传入参数为null，但是目标参数类型为原始类型，做转换
        // 5、传入参数类型不对应，尝试转换类型
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Object[] actualArgs = new Object[parameterTypes.length];
        if (null != args) {
            for (int i = 0; i < actualArgs.length; i++) {
                if (i >= args.length || null == args[i]) {
                    // 越界或者空值
                    actualArgs[i] = ClassUtil.getDefaultValue(parameterTypes[i]);
                } else if (args[i] instanceof NullWrapperBean) {
                    //如果是通过NullWrapperBean传递的null参数,直接赋值null
                    actualArgs[i] = null;
                } else if (false == parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    //对于类型不同的字段，尝试转换，转换失败则使用原对象类型
                    final Object targetValue = Convert.convertWithCheck(parameterTypes[i], args[i], null, true);
                    if (null != targetValue) {
                        actualArgs[i] = targetValue;
                    } else {
                        actualArgs[i] = args[i];
                    }
                } else {
                    actualArgs[i] = args[i];
                }
            }
        }

        if (method.isDefault()) {
            // 当方法是default方法时，尤其对象是代理对象，需使用句柄方式执行
            // 代理对象情况下调用method.invoke会导致循环引用执行，最终栈溢出
            return MethodHandleUtil.invokeSpecial(obj, method, args);
        }

        return (T) method.invoke(ClassUtil.isStatic(method) ? null : obj, actualArgs);
    }

    /**
     * 执行对象中指定方法
     * 如果需要传递的参数为null,请使用NullWrapperBean来传递,不然会丢失类型信息
     *
     * @param <T>        返回对象类型
     * @param obj        方法所在对象
     * @param methodName 方法名
     * @param args       参数列表
     * @return 执行结果
     * @throws UtilException IllegalAccessException等异常包装
     * @see NullWrapperBean
     * @since 3.1.2
     */
    public static <T> T invoke(Object obj, String methodName, Object... args) throws UtilException {
        Assert.notNull(obj, "Object to get method must be not null!");
        Assert.notBlank(methodName, "Method name must be not blank!");

        final Method method = getMethodOfObj(obj, methodName, args);
        if (null == method) {
            throw new UtilException("No such method: [{}] from [{}]", methodName, obj.getClass());
        }
        return invoke(obj, method, args);
    }

    /**
     * 设置方法为可访问（私有方法可以被外部调用）
     *
     * @param <T>              AccessibleObject的子类，比如Class、Method、Field等
     * @param accessibleObject 可设置访问权限的对象，比如Class、Method、Field等
     * @return 被设置可访问的对象
     * @since 4.6.8
     */
    public static <T extends AccessibleObject> T setAccessible(T accessibleObject) {
        if (null != accessibleObject && false == accessibleObject.isAccessible()) {
            accessibleObject.setAccessible(true);
        }
        return accessibleObject;
    }

    /**
     * 设置final的field字段可以被修改
     * 只要不会被编译器内联优化的 final 属性就可以通过反射有效的进行修改 --  修改后代码中可使用到新的值;
     * <p>以下属性，编译器会内联优化，无法通过反射修改：</p>
     * <ul>
     *     <li> 基本类型 byte, char, short, int, long, float, double, boolean</li>
     *     <li> Literal String 类型(直接双引号字符串)</li>
     * </ul>
     * <p>以下属性，可以通过反射修改：</p>
     * <ul>
     *     <li>基本类型的包装类 Byte、Character、Short、Long、Float、Double、Boolean</li>
     *     <li>字符串，通过 new String("")实例化</li>
     *     <li>自定义java类</li>
     * </ul>
     * <pre class="code">
     * {@code
     *      //示例，移除final修饰符
     *      class JdbcDialects {private static final List<Number> dialects = new ArrayList<>();}
     *      Field field = ReflectUtil.getField(JdbcDialects.class, fieldName);
     * 		ReflectUtil.removeFinalModify(field);
     * 		ReflectUtil.setFieldValue(JdbcDialects.class, fieldName, dialects);
     *    }
     * </pre>
     *
     * @param field 被修改的field，不可以为空
     * @throws UtilException IllegalAccessException等异常包装
     * @author dazer
     * @since 5.8.8
     */
    public static void removeFinalModify(Field field) {
        ModifierUtil.removeFinalModify(field);
    }

    /**
     * 获取方法的唯一键，结构为:
     * <pre>
     *     返回类型#方法名:参数1类型,参数2类型...
     * </pre>
     *
     * @param method 方法
     * @return 方法唯一键
     */
    private static String getUniqueKey(Method method) {
        final StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName()).append('#');
        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(':');
            } else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    /**
     * 获取类对应接口中的非抽象方法（default方法）
     *
     * @param clazz 类
     * @return 方法列表
     */
    private static List<Method> getDefaultMethodsFromInterface(Class<?> clazz) {
        List<Method> result = new ArrayList<>();
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method m : ifc.getMethods()) {
                if (false == ModifierUtil.isAbstract(m)) {
                    result.add(m);
                }
            }
        }
        return result;
    }

}
