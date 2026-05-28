package com.example.blog.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditLogAspect {
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    @AfterReturning("within(com.example.blog.controller.AdminController) || within(com.example.blog.controller.AuthController)")
    public void logAdminAction(JoinPoint point) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attrs == null ? null : attrs.getRequest();
        String user = SecurityContextHolder.getContext().getAuthentication() == null ? "anonymous" : SecurityContextHolder.getContext().getAuthentication().getName();
        audit.info("user={} method={} uri={} action={}", user, req == null ? "" : req.getMethod(), req == null ? "" : req.getRequestURI(), point.getSignature().toShortString());
    }
}
