package com.tianxin.platform.auth;

import com.tianxin.platform.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthService.LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    public ApiResponse<AuthService.UserView> me(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("currentUserId");
        return ApiResponse.success(authService.toUserView(authService.requireUser(userId)));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, Boolean>> logout() {
        return ApiResponse.success(Map.of("loggedOut", true));
    }

    public record LoginRequest(
            @NotBlank(message = "用户名不能为空") @Size(max = 32, message = "用户名不能超过32位") String username,
            @NotBlank(message = "密码不能为空") @Size(max = 128, message = "密码不能超过128位") String password) { }
}
