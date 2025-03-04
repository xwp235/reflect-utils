package jp.onehr.reflect.object;

import jp.onehr.reflect.array.ArrayUtils;
import jp.onehr.reflect.instance.InvokeReflectUtils;
import jp.onehr.reflect.number.NumberUtils;
import jp.onehr.reflect.serialize.SerializeUtils;

import java.util.Objects;

/**
 * 对象工具类，包括判空、克隆、序列化等操作
 */
public class ObjectUtils {

    /**
     * 比较两个对象是否相等，此方法是 {@link #equal(Object, Object)}的别名方法。<br>
     * 相同的条件有两个，满足其一即可：<br>
     * <ol>
     * <li>obj1 == null &amp;&amp; obj2 == null</li>
     * <li>obj1.equals(obj2)</li>
     * <li>如果是BigDecimal比较，0 == obj1.compareTo(obj2)</li>
     * </ol>
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     * @see #equal(Object, Object)
     */
    public static boolean equals(Object obj1, Object obj2) {
        return equal(obj1, obj2);
    }

    /**
     * 比较两个对象是否相等。<br>
     * 相同的条件有两个，满足其一即可：<br>
     * <ol>
     * <li>obj1 == null &amp;&amp; obj2 == null</li>
     * <li>obj1.equals(obj2)</li>
     * <li>如果是BigDecimal比较，0 == obj1.compareTo(obj2)</li>
     * </ol>
     *
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     * @see Objects#equals(Object, Object)
     */
    public static boolean equal(Object obj1, Object obj2) {
        if (obj1 instanceof Number o1 && obj2 instanceof Number o2) {
            return NumberUtils.equals(o1, o2);
        }
        return Objects.equals(obj1, obj2);
    }

    /**
     * 克隆对象<br>
     * 如果对象实现Cloneable接口，调用其clone方法<br>
     * 如果实现Serializable接口，执行深度克隆<br>
     * 否则返回{@code null}
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     */
    public static <T> T clone(T obj) {
        T result = ArrayUtils.clone(obj);
        if (null == result) {
            if (obj instanceof Cloneable) {
                result = InvokeReflectUtils.invoke(obj, "clone");
            } else {
                result = cloneByStream(obj);
            }
        }
        return result;
    }

    /**
     * 序列化后拷贝流的方式克隆<br>
     * 对象必须实现Serializable接口
     *
     * @param <T> 对象类型
     * @param obj 被克隆对象
     * @return 克隆后的对象
     * @throws UtilException IO异常和ClassNotFoundException封装
     */
    public static <T> T cloneByStream(T obj) {
        return SerializeUtils.clone(obj);
    }

}
