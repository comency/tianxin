package com.tianxin.platform.system.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditLogInterceptor implements HandlerInterceptor {

    private static final String STARTED_AT_ATTRIBUTE = "auditStartedAt";
    private final AuditLogStore auditLogStore;

    public AuditLogInterceptor(AuditLogStore auditLogStore) {
        this.auditLogStore = auditLogStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(STARTED_AT_ATTRIBUTE, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        Object startedAt = request.getAttribute(STARTED_AT_ATTRIBUTE);
        long durationMs = startedAt instanceof Long start ? (System.nanoTime() - start) / 1_000_000 : 0;
        Object currentUserId = request.getAttribute("currentUserId");
        UUID operatorId = currentUserId instanceof UUID id ? id : null;
        auditLogStore.append(new AuditLog(UUID.randomUUID(), Instant.now(), operatorId, request.getMethod(),
                request.getRequestURI(), exception == null && response.getStatus() < 400, durationMs));
    }
}
