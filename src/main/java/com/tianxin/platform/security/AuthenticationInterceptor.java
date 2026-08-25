package com.tianxin.platform.security;

import com.tianxin.platform.auth.AuthService;
import com.tianxin.platform.common.api.ApiResponse;
import com.tianxin.platform.system.model.SystemUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenService tokenService;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthenticationInterceptor(JwtTokenService tokenService, AuthService authService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeFailure(response, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "请先登录");
            return false;
        }
        String subject = tokenService.verifyAndGetSubject(header.substring(7)).orElse(null);
        if (subject == null) {
            writeFailure(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "登录状态已失效");
            return false;
        }
        SystemUser user;
        try {
            user = authService.requireUser(UUID.fromString(subject));
        } catch (IllegalArgumentException exception) {
            writeFailure(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_USER", "用户不存在或已停用");
            return false;
        }
        String permission = requiredPermission(request);
        if (permission != null && !authService.hasPermission(user, permission)) {
            writeFailure(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "没有该操作权限");
            return false;
        }
        request.setAttribute("currentUserId", user.id());
        return true;
    }

    private String requiredPermission(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/system/users")) {
            return "GET".equals(request.getMethod()) ? "sys:user:read" : "sys:user:write";
        }
        if (path.startsWith("/api/v1/system/roles")) {
            return "sys:role:read";
        }
        if (path.startsWith("/api/v1/system/menus")) {
            return "sys:menu:read";
        }
        if (path.startsWith("/api/v1/system/departments")) {
            return "sys:dept:read";
        }
        if (path.startsWith("/api/v1/system/dictionaries")) {
            return "sys:dict:read";
        }
        if (path.startsWith("/api/v1/system/audit-logs")) {
            return "sys:audit:read";
        }
        return null;
    }

    private void writeFailure(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiResponse.failure(code, message));
    }
}
