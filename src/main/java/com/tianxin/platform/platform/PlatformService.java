package com.tianxin.platform.platform;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PlatformService {

    private final String applicationName;
    private final String applicationVersion;

    public PlatformService(
            @Value("${spring.application.name}") String applicationName,
            @Value("${tx.platform.version}") String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    public Map<String, Object> overview() {
        return Map.of(
                "application", applicationName,
                "version", applicationVersion,
                "status", "RUNNING",
                "startedAt", Instant.now(),
                "modules", List.of("系统管理", "行业资讯", "标准规范", "AI咨询", "项目管理"));
    }
}
