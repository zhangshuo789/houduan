package com.volleyball.volleyballcommunitybackend.aspect;

import com.volleyball.volleyballcommunitybackend.annotation.AdminOperation;
import com.volleyball.volleyballcommunitybackend.entity.AdminLog;
import com.volleyball.volleyballcommunitybackend.repository.AdminLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class AdminLogAspect {

    private static final Logger logger = LoggerFactory.getLogger("com.volleyball.volleyballcommunitybackend.aspect");

    private final AdminLogRepository adminLogRepository;

    public AdminLogAspect(AdminLogRepository adminLogRepository) {
        this.adminLogRepository = adminLogRepository;
    }

    @Around("@annotation(adminOperation)")
    public Object logAdminOperation(ProceedingJoinPoint joinPoint, AdminOperation adminOperation) throws Throwable {
        // 获取当前请求
        HttpServletRequest request = getRequest();
        String ip = request != null ? getClientIp(request) : "unknown";

        // 从 SecurityContext 获取 adminId
        Long adminId = getCurrentUserId();

        // 执行方法
        Object result = joinPoint.proceed();

        // 方法成功后记录 AdminLog
        try {
            AdminLog logEntry = new AdminLog();
            logEntry.setAdminId(adminId);
            logEntry.setAction(adminOperation.action());
            logEntry.setTargetType(adminOperation.targetType());
            logEntry.setTargetId(extractTargetId(joinPoint));
            logEntry.setDetail(buildDetail(joinPoint, adminOperation));
            logEntry.setIp(ip);
            adminLogRepository.save(logEntry);
        } catch (Exception e) {
            logger.error("Failed to save admin log", e);
        }

        return result;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private Long extractTargetId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        // 假设 targetId 是第一个 Long 类型参数
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    private String buildDetail(ProceedingJoinPoint joinPoint, AdminOperation adminOperation) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName + " - " + adminOperation.action();
    }
}
