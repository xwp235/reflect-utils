package jp.onehr.reflect.text;

public class CharSequenceUtils {

    /**
     * <p>字符串是否为空白，空白的定义如下：</p>
     * <ol>
     *     <li>{@code null}</li>
     *     <li>空字符串：{@code ""}</li>
     *     <li>空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isBlank(null)     // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank("")       // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank(" \t\n")  // true}</li>
     *     <li>{@code CharSequenceUtil.isBlank("abc")    // false}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isEmpty(CharSequence)} 略慢。</p>
     * <br>
     *
     * <p>建议：</p>
     * <ul>
     *     <li>该方法建议仅对于客户端（或第三方接口）传入的参数使用该方法。</li>
     *     <li>需要同时校验多个字符串时，建议采用 {@link #hasBlank(CharSequence...)} 或 {@link #isAllBlank(CharSequence...)}</li>
     * </ul>
     *
     * @param str 被检测的字符串
     * @return 若为空白，则返回 true
     * @see #isEmpty(CharSequence)
     */
    public static boolean isBlank(CharSequence str) {
        final int length;
        if ((str == null) || ((length = str.length()) == 0)) {
            return true;
        }

        for (int i = 0; i < length; i++) {
            // 只要有一个非空字符即为非空字符串
            if (!CharUtils.isBlankChar(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>字符串是否为非空白，非空白的定义如下： </p>
     * <ol>
     *     <li>不为 {@code null}</li>
     *     <li>不为空字符串：{@code ""}</li>
     *     <li>不为空格、全角空格、制表符、换行符，等不可见字符</li>
     * </ol>
     *
     * <p>例：</p>
     * <ul>
     *     <li>{@code CharSequenceUtil.isNotBlank(null)     // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank("")       // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank(" \t\n")  // false}</li>
     *     <li>{@code CharSequenceUtil.isNotBlank("abc")    // true}</li>
     * </ul>
     *
     * <p>注意：该方法与 {@link #isNotEmpty(CharSequence)} 的区别是：
     * 该方法会校验空白字符，且性能相对于 {@link #isNotEmpty(CharSequence)} 略慢。</p>
     * <p>建议：仅对于客户端（或第三方接口）传入的参数使用该方法。</p>
     *
     * @param str 被检测的字符串
     * @return 是否为非空
     * @see #isBlank(CharSequence)
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

}
