package jp.onehr.reflect.exceptions;

public class ExceptionUtils {

    /**
     * 使用运行时异常包装编译异常<br>
     * <p>
     * 如果传入参数已经是运行时异常，则直接返回，不再额外包装
     *
     * @param throwable 异常
     * @return 运行时异常
     */
    public static RuntimeException wrapRuntime(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(throwable);
    }

}
