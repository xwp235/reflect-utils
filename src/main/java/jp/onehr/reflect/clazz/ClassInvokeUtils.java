package jp.onehr.reflect.clazz;

public class ClassInvokeUtils {

    /**
     * 执行方法<br>
     * 可执行Private方法，也可执行static方法<br>
     * 执行非static方法时，必须满足对象有默认构造方法<br>
     * 非单例模式，如果是非静态方法，每次创建一个新对象
     *
     * @param <T>                     对象类型
     * @param classNameWithMethodName 类名和方法名表达式，类名与方法名用{@code .}或{@code #}连接 例如：com.xiaoleilu.hutool.StrUtil.isEmpty 或 com.xiaoleilu.hutool.StrUtil#isEmpty
     * @param args                    参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(String classNameWithMethodName, Object[] args) {
        return invoke(classNameWithMethodName, false, args);
    }

    /**
     * 执行方法<br>
     * 可执行Private方法，也可执行static方法<br>
     * 执行非static方法时，必须满足对象有默认构造方法<br>
     *
     * @param <T>                     对象类型
     * @param classNameWithMethodName 类名和方法名表达式，例如：com.xiaoleilu.hutool.StrUtil#isEmpty或com.xiaoleilu.hutool.StrUtil.isEmpty
     * @param isSingleton             是否为单例对象，如果此参数为false，每次执行方法时创建一个新对象
     * @param args                    参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(String classNameWithMethodName, boolean isSingleton, Object... args) {
        if (StrUtil.isBlank(classNameWithMethodName)) {
            throw new UtilException("Blank classNameDotMethodName!");
        }

        int splitIndex = classNameWithMethodName.lastIndexOf('#');
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf('.');
        }
        if (splitIndex <= 0) {
            throw new UtilException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        final String className = classNameWithMethodName.substring(0, splitIndex);
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);

        return invoke(className, methodName, isSingleton, args);
    }

    /**
     * 执行方法<br>
     * 可执行Private方法，也可执行static方法<br>
     * 执行非static方法时，必须满足对象有默认构造方法<br>
     * 非单例模式，如果是非静态方法，每次创建一个新对象
     *
     * @param <T>        对象类型
     * @param className  类名，完整类路径
     * @param methodName 方法名
     * @param args       参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(String className, String methodName, Object[] args) {
        return invoke(className, methodName, false, args);
    }

    /**
     * 执行方法<br>
     * 可执行Private方法，也可执行static方法<br>
     * 执行非static方法时，必须满足对象有默认构造方法<br>
     *
     * @param <T>         对象类型
     * @param className   类名，完整类路径
     * @param methodName  方法名
     * @param isSingleton 是否为单例对象，如果此参数为false，每次执行方法时创建一个新对象
     * @param args        参数，必须严格对应指定方法的参数类型和数量
     * @return 返回结果
     */
    public static <T> T invoke(String className, String methodName, boolean isSingleton, Object... args) {
        Class<Object> clazz = loadClass(className);
        try {
            final Method method = getDeclaredMethod(clazz, methodName, getClasses(args));
            if (null == method) {
                throw new NoSuchMethodException(StrUtil.format("No such method: [{}]", methodName));
            }
            if (isStatic(method)) {
                return ReflectUtil.invoke(null, method, args);
            } else {
                return ReflectUtil.invoke(isSingleton ? Singleton.get(clazz) : clazz.newInstance(), method, args);
            }
        } catch (Exception e) {
            throw new UtilException(e);
        }
    }

    
}
