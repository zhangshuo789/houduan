/**
 * @description 日志工具类（集成敏感信息过滤）
 * @author lihao
 * @date 2026/4/4
 */
package com.volleyball.volleyballcommunitybackend.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 日志工具类，提供敏感信息自动过滤的日志记录方法
 */
public class LogUtils {

    private static final SensitiveDataFilter filter = new SensitiveDataFilter();

    /**
     * 获取指定名称的Logger
     */
    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    /**
     * 获取调用类的Logger
     */
    public static Logger getLogger() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = stackTrace[stackTrace.length > 2 ? 2 : stackTrace.length - 1].getClassName();
        return LoggerFactory.getLogger(className);
    }

    // ==================== INFO 级别 ====================

    public static void info(Logger logger, String format, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(format, maskArgs(args));
        }
    }

    public static void info(Logger logger, String msg) {
        if (logger.isInfoEnabled()) {
            logger.info(msg);
        }
    }

    public static void info(Logger logger, String msg, Object obj) {
        if (logger.isInfoEnabled()) {
            logger.info(msg + ": {}", filter.maskObject(obj));
        }
    }

    public static void infoKeyValue(Logger logger, String key, Object value) {
        if (logger.isInfoEnabled()) {
            boolean isSensitive = filter.isSensitiveField(key);
            Object val = isSensitive ? "***" : value;
            logger.info("{} = {}", key, val);
        }
    }

    // ==================== DEBUG 级别 ====================

    public static void debug(Logger logger, String format, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(format, maskArgs(args));
        }
    }

    public static void debug(Logger logger, String msg) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg);
        }
    }

    public static void debug(Logger logger, String msg, Object obj) {
        if (logger.isDebugEnabled()) {
            logger.debug(msg + ": {}", filter.maskObject(obj));
        }
    }

    public static void debugKeyValue(Logger logger, String key, Object value) {
        if (logger.isDebugEnabled()) {
            boolean isSensitive = filter.isSensitiveField(key);
            Object val = isSensitive ? "***" : value;
            logger.debug("{} = {}", key, val);
        }
    }

    // ==================== WARN 级别 ====================

    public static void warn(Logger logger, String format, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(format, maskArgs(args));
        }
    }

    public static void warn(Logger logger, String msg) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg);
        }
    }

    public static void warn(Logger logger, String msg, Object obj) {
        if (logger.isWarnEnabled()) {
            logger.warn(msg + ": {}", filter.maskObject(obj));
        }
    }

    // ==================== ERROR 级别 ====================

    public static void error(Logger logger, String format, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(format, maskArgs(args));
        }
    }

    public static void error(Logger logger, String msg) {
        if (logger.isErrorEnabled()) {
            logger.error(msg);
        }
    }

    public static void error(Logger logger, String msg, Throwable t) {
        if (logger.isErrorEnabled()) {
            logger.error(msg, t);
        }
    }

    public static void error(Logger logger, Throwable t, String format, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(format, maskArgs(args));
            logger.error("Exception: ", t);
        }
    }

    // ==================== MDC 相关 ====================

    /**
     * 设置MDC上下文（用于链路追踪）
     */
    public static void setMdc(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * 设置MDC上下文（敏感值会自动脱敏）
     */
    public static void setMdcSensitive(String key, String value) {
        boolean isSensitive = filter.isSensitiveField(key);
        MDC.put(key, isSensitive ? "***" : value);
    }

    /**
     * 清除MDC上下文
     */
    public static void clearMdc() {
        MDC.clear();
    }

    /**
     * 移除MDC中的指定key
     */
    public static void removeMdc(String key) {
        MDC.remove(key);
    }

    /**
     * 批量设置MDC
     */
    public static void putMdc(Map<String, String> mdcMap) {
        mdcMap.forEach(MDC::put);
    }

    // ==================== 私有方法 ====================

    /**
     * 对参数数组中的敏感信息进行脱敏
     */
    private static Object[] maskArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] masked = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                masked[i] = null;
            } else if (arg instanceof String) {
                // 字符串需要检查是否包含敏感模式
                masked[i] = filter.maskJson((String) arg);
            } else if (isPrimitiveOrWrapper(arg.getClass())) {
                masked[i] = arg;
            } else {
                // 对象类型，转换为JSON并脱敏
                masked[i] = filter.maskObject(arg);
            }
        }
        return masked;
    }

    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz == Boolean.class ||
               clazz == Character.class ||
               clazz == Byte.class ||
               clazz == Short.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Float.class ||
               clazz == Double.class ||
               clazz == String.class ||
               clazz == Void.class;
    }

    /**
     * 通用脱敏方法
     */
    public static String mask(String value, String type) {
        return filter.mask(value, type);
    }

    /**
     * JSON字符串脱敏
     */
    public static String maskJson(String json) {
        return filter.maskJson(json);
    }

    /**
     * 对象脱敏
     */
    public static String maskObject(Object obj) {
        return filter.maskObject(obj);
    }

    /**
     * 判断字段是否为敏感字段
     */
    public static boolean isSensitive(String fieldName) {
        return filter.isSensitiveField(fieldName);
    }
}
