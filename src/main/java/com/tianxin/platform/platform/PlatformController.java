package com.tianxin.platform.platform;

import com.tianxin.platform.common.api.ApiResponse;
import java.util.Map;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
public class PlatformController {

    private final PlatformService platformService;
    private final RedisConnectionFactory redisConnectionFactory;

    public PlatformController(PlatformService platformService, RedisConnectionFactory redisConnectionFactory) {
        this.platformService = platformService;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(platformService.overview());
    }

    @GetMapping("/readiness")
    public ApiResponse<Map<String, String>> readiness() {
        try (var connection = redisConnectionFactory.getConnection()) {
            connection.ping();
            return ApiResponse.success(Map.of("application", "UP", "redis", "UP"));
        } catch (Exception exception) {
            return ApiResponse.success(Map.of("application", "UP", "redis", "DOWN"));
        }
    }
}
