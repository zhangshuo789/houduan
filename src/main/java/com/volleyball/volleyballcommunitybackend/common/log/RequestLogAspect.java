/**
 * @description 请求响应日志记录（已集成敏感信息过滤）
 * @author lihao
 * @date 2026/4/4
 */
package com.volleyball.volleyballcommunitybackend.common.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
public class RequestLogAspect {

    private static final Logger logger = LoggerFactory.getLogger("com.volleyball.volleyballcommunitybackend.common.log");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensitiveDataFilter sensitiveFilter = new SensitiveDataFilter();

    // 额外需要脱敏的字段名（不区分大小写）
    private static final Set<String> EXTRA_SENSITIVE_FIELDS = new HashSet<>();
    static {
        // 登录相关
        EXTRA_SENSITIVE_FIELDS.add("username");
        EXTRA_SENSITIVE_FIELDS.add("userName");
        EXTRA_SENSITIVE_FIELDS.add("account");
        EXTRA_SENSITIVE_FIELDS.add("loginName");
        EXTRA_SENSITIVE_FIELDS.add("loginname");
        // 金额/财务相关
        EXTRA_SENSITIVE_FIELDS.add("amount");
        EXTRA_SENSITIVE_FIELDS.add("balance");
        EXTRA_SENSITIVE_FIELDS.add("money");
        EXTRA_SENSITIVE_FIELDS.add("salary");
        // 地址
        EXTRA_SENSITIVE_FIELDS.add("address");
        EXTRA_SENSITIVE_FIELDS.add("homeAddress");
        EXTRA_SENSITIVE_FIELDS.add("workAddress");
        // 其他
        EXTRA_SENSITIVE_FIELDS.add("birthday");
        EXTRA_SENSITIVE_FIELDS.add("birthDate");
    }

    @Pointcut("execution(* com.volleyball.volleyballcommunitybackend..controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = getRequest();
        String uri = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";
        String controllerClass = point.getSignature().getDeclaringTypeName();
        String methodName = point.getSignature().getName();

        // 记录请求参数 (INFO级别)
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("uri", uri);
        requestInfo.put("method", method);
        requestInfo.put("class", controllerClass);
        requestInfo.put("function", methodName);
        requestInfo.put("params", getParams(point.getArgs()));

        logger.info(">>> REQUEST: {} {} | {}", method, uri, toJson(requestInfo));

        Object result = null;
        Throwable error = null;
        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            if (error != null) {
                logger.info("<<< RESPONSE ERROR: {} | {} | {}ms | error: {}",
                    method, uri, costTime, error.getClass().getSimpleName());
            } else {
                // DEBUG级别记录完整响应
                logger.debug("<<< RESPONSE: {} {} | {}ms | result: {}",
                    method, uri, costTime, maskSensitiveData(toJson(result)));
            }
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private Map<String, Object> getParams(Object[] args) {
        Map<String, Object> params = new HashMap<>();
        if (args == null || args.length == 0) {
            return params;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                params.put("arg[" + i + "]", null);
            } else if (isSimpleType(arg)) {
                params.put("arg[" + i + "]", maskValueIfSensitive(null, arg));
            } else if (arg instanceof MultipartFile) {
                params.put("arg[" + i + "]", "MultipartFile");
            } else if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                params.put("arg[" + i + "]", arg.getClass().getSimpleName());
            } else {
                try {
                    String json = toJson(arg);
                    params.put("arg[" + i + "]", maskSensitiveFields(arg.getClass().getSimpleName(), json));
                } catch (Exception e) {
                    params.put("arg[" + i + "]", arg.getClass().getSimpleName());
                }
            }
        }
        return params;
    }

    private boolean isSimpleType(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character;
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    /**
     * 如果字段名是敏感字段，对值进行脱敏
     */
    private Object maskValueIfSensitive(String fieldName, Object value) {
        if (fieldName != null && sensitiveFilter.isSensitiveField(fieldName)) {
            return "***";
        }
        return value;
    }

    /**
     * 对JSON中的敏感字段进行脱敏
     */
    private String maskSensitiveFields(String className, String json) {
        // 先用正则过滤自动发现的敏感信息（手机号、身份证等）
        String masked = sensitiveFilter.maskJson(json);

        // 遍历额外需要脱敏的字段
        for (String field : EXTRA_SENSITIVE_FIELDS) {
            // 匹配 "fieldName": "value" 或 "fieldName":value 的模式
            String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
            masked = masked.replaceAll(pattern, "\"" + field + "\":\"***\"");
            pattern = "\"" + field + "\"\\s*:\\s*(\\d+)";
            masked = masked.replaceAll(pattern, "\"" + field + "\":***");
        }

        return masked;
    }

    /**
     * 过滤响应中的敏感数据
     */
    private String maskSensitiveData(String json) {
        if (json == null) {
            return null;
        }
        return maskSensitiveFields("Response", json);
    }
}
