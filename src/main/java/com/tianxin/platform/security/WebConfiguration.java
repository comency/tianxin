package com.tianxin.platform.security;

import org.springframework.context.annotation.Configuration;
import com.tianxin.platform.system.audit.AuditLogInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final AuditLogInterceptor auditLogInterceptor;

    public WebConfiguration(AuthenticationInterceptor authenticationInterceptor, AuditLogInterceptor auditLogInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.auditLogInterceptor = auditLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/v1/auth/**", "/api/v1/system/**")
                .excludePathPatterns("/api/v1/auth/login");
        registry.addInterceptor(auditLogInterceptor)
                .addPathPatterns("/api/v1/system/**");
    }
}
