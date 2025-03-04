package jp.onehr.reflect.clazz;

import jp.onehr.reflect.text.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ClasspathUtils {

    /**
     * 获得ClassPath，不解码路径中的特殊字符（例如空格和中文）
     *
     * @return ClassPath集合
     */
    public static Set<String> getClassPathResources() {
        return getClassPathResources(false);
    }

    /**
     * 获得ClassPath
     *
     * @param isDecode 是否解码路径中的特殊字符（例如空格和中文）
     * @return ClassPath集合
     * @since 4.0.11
     */
    public static Set<String> getClassPathResources(boolean isDecode) {
        return getClassPaths(StringUtils.EMPTY, isDecode);
    }

    /**
     * 获得ClassPath，不解码路径中的特殊字符（例如空格和中文）
     *
     * @param packageName 包名称
     * @return ClassPath路径字符串集合
     */
    public static Set<String> getClassPaths(String packageName) {
        return getClassPaths(packageName, false);
    }

    /**
     * 获得ClassPath
     *
     * @param packageName 包名称
     * @param isDecode    是否解码路径中的特殊字符（例如空格和中文）
     * @return ClassPath路径字符串集合
     * @since 4.0.11
     */
    public static Set<String> getClassPaths(String packageName, boolean isDecode) {
        String packagePath = packageName.replace(StrUtil.DOT, StrUtil.SLASH);
        Enumeration<URL> resources;
        try {
            resources = getClassLoader().getResources(packagePath);
        } catch (IOException e) {
            throw new UtilException(e, "Loading classPath [{}] error!", packagePath);
        }
        final Set<String> paths = new HashSet<>();
        String path;
        while (resources.hasMoreElements()) {
            path = resources.nextElement().getPath();
            paths.add(isDecode ? URLUtil.decode(path, CharsetUtil.systemCharsetName()) : path);
        }
        return paths;
    }

    /**
     * 获得ClassPath，将编码后的中文路径解码为原字符<br>
     * 这个ClassPath路径会文件路径被标准化处理
     *
     * @return ClassPath
     */
    public static String getClassPath() {
        return getClassPath(false);
    }

    /**
     * 获得ClassPath，这个ClassPath路径会文件路径被标准化处理
     *
     * @param isEncoded 是否编码路径中的中文
     * @return ClassPath
     * @since 3.2.1
     */
    public static String getClassPath(boolean isEncoded) {
        final URL classPathURL = getClassPathURL();
        String url = isEncoded ? classPathURL.getPath() : URLUtil.getDecodedPath(classPathURL);
        return FileUtil.normalize(url);
    }

    /**
     * 获得ClassPath URL
     *
     * @return ClassPath URL
     */
    public static URL getClassPathURL() {
        return getResourceURL(StrUtil.EMPTY);
    }

    /**
     * 获得资源的URL<br>
     * 路径用/分隔，例如:
     *
     * <pre>
     * config/a/db.config
     * spring/xml/test.xml
     * </pre>
     *
     * @param resource 资源（相对Classpath的路径）
     * @return 资源URL
     * @see ResourceUtil#getResource(String)
     */
    public static URL getResourceURL(String resource) throws IORuntimeException {
        return ResourceUtil.getResource(resource);
    }

    /**
     * 获取指定路径下的资源列表<br>
     * 路径格式必须为目录格式,用/分隔，例如:
     *
     * <pre>
     * config/a
     * spring/xml
     * </pre>
     *
     * @param resource 资源路径
     * @return 资源列表
     * @see ResourceUtil#getResources(String)
     */
    public static List<URL> getResources(String resource) {
        return ResourceUtil.getResources(resource);
    }

    /**
     * 获得资源相对路径对应的URL
     *
     * @param resource  资源相对路径
     * @param baseClass 基准Class，获得的相对路径相对于此Class所在路径，如果为{@code null}则相对ClassPath
     * @return {@link URL}
     * @see ResourceUtil#getResource(String, Class)
     */
    public static URL getResourceUrl(String resource, Class<?> baseClass) {
        return ResourceUtil.getResource(resource, baseClass);
    }

    /**
     * @return 获得Java ClassPath路径，不包括 jre
     */
    public static String[] getJavaClassPaths() {
        return System.getProperty("java.class.path").split(System.getProperty("path.separator"));
    }

    /**
     * 获取当前线程的{@link ClassLoader}
     *
     * @return 当前线程的class loader
     * @see ClassLoaderUtil#getClassLoader()
     */
    public static ClassLoader getContextClassLoader() {
        return ClassLoaderUtil.getContextClassLoader();
    }

    /**
     * 获取{@link ClassLoader}<br>
     * 获取顺序如下：<br>
     *
     * <pre>
     * 1、获取当前线程的ContextClassLoader
     * 2、获取{@link ClassLoaderUtil}类对应的ClassLoader
     * 3、获取系统ClassLoader（{@link ClassLoader#getSystemClassLoader()}）
     * </pre>
     *
     * @return 类加载器
     */
    public static ClassLoader getClassLoader() {
        return ClassLoaderUtil.getClassLoader();
    }

    /**
     * 比较判断types1和types2两组类，如果types1中所有的类都与types2对应位置的类相同，或者是其父类或接口，则返回{@code true}
     *
     * @param types1 类组1
     * @param types2 类组2
     * @return 是否相同、父类或接口
     */
    public static boolean isAllAssignableFrom(Class<?>[] types1, Class<?>[] types2) {
        if (ArrayUtil.isEmpty(types1) && ArrayUtil.isEmpty(types2)) {
            return true;
        }
        if (null == types1 || null == types2) {
            // 任何一个为null不相等（之前已判断两个都为null的情况）
            return false;
        }
        if (types1.length != types2.length) {
            return false;
        }

        Class<?> type1;
        Class<?> type2;
        for (int i = 0; i < types1.length; i++) {
            type1 = types1[i];
            type2 = types2[i];
            if (isBasicType(type1) && isBasicType(type2)) {
                // 原始类型和包装类型存在不一致情况
                if (BasicType.unWrap(type1) != BasicType.unWrap(type2)) {
                    return false;
                }
            } else if (false == type1.isAssignableFrom(type2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 加载类
     *
     * @param <T>           对象类型
     * @param className     类名
     * @param isInitialized 是否初始化
     * @return 类
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className, boolean isInitialized) {
        return (Class<T>) ClassLoaderUtil.loadClass(className, isInitialized);
    }

    /**
     * 加载类并初始化
     *
     * @param <T>       对象类型
     * @param className 类名
     * @return 类
     */
    public static <T> Class<T> loadClass(String className) {
        return loadClass(className, true);
    }

}
