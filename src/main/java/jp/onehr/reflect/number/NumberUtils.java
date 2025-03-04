package jp.onehr.reflect.number;

import java.math.BigDecimal;
import java.util.Objects;

public class NumberUtils {

    /**
     * 比较数字值是否相等，相等返回{@code true}<br>
     * 需要注意的是{@link BigDecimal}需要特殊处理<br>
     * BigDecimal使用compareTo方式判断，因为使用equals方法也判断小数位数，如2.0和2.00就不相等，<br>
     * 此方法判断值相等时忽略精度的，即0.00 == 0
     *
     * <ul>
     *     <li>如果用户提供两个Number都是{@link BigDecimal}，则通过调用{@link BigDecimal#compareTo(BigDecimal)}方法来判断是否相等</li>
     *     <li>其他情况调用{@link Number#equals(Object)}比较</li>
     * </ul>
     *
     * @param number1 数字1
     * @param number2 数字2
     * @return 是否相等
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(final Number number1, final Number number2) {
        if (number1 instanceof BigDecimal n1 && number2 instanceof BigDecimal n2) {
            // BigDecimal使用compareTo方式判断，因为使用equals方法也判断小数位数，如2.0和2.00就不相等
            return equals(n1, n2);
        }
        return Objects.equals(number1, number2);
    }

    /**
     * 比较大小，值相等 返回true<br>
     * 此方法通过调用{@link BigDecimal#compareTo(BigDecimal)}方法来判断是否相等<br>
     * 此方法判断值相等时忽略精度的，即0.00 == 0
     *
     * @param bigNum1 数字1
     * @param bigNum2 数字2
     * @return 是否相等
     */
    public static boolean equals(BigDecimal bigNum1, BigDecimal bigNum2) {
        //noinspection NumberEquality
        if (bigNum1 == bigNum2) {
            // 如果用户传入同一对象，省略compareTo以提高性能。
            return true;
        }
        if (bigNum1 == null || bigNum2 == null) {
            return false;
        }
        return 0 == bigNum1.compareTo(bigNum2);
    }

}
