package com.tianxin.platform.system;

import com.tianxin.platform.auth.AuthService;
import com.tianxin.platform.common.api.ApiResponse;
import com.tianxin.platform.system.model.SystemUser;
import com.tianxin.platform.system.store.IdentityStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/users")
public class SystemUserController {

    private final IdentityStore identityStore;
    private final AuthService authService;

    public SystemUserController(IdentityStore identityStore, AuthService authService) {
        this.identityStore = identityStore;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<AuthService.UserView>> list() {
        return ApiResponse.success(identityStore.listUsers().stream().map(authService::toUserView).toList());
    }

    @PostMapping
    public ApiResponse<AuthService.UserView> create(@Valid @RequestBody CreateUserRequest request) {
        SystemUser user = identityStore.createUser(request.username(), request.displayName(), request.password(), request.roleCodes());
        return ApiResponse.success(authService.toUserView(user));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request) {
        identityStore.updateUserStatus(id, request.enabled());
        return ApiResponse.success(null);
    }

    public record CreateUserRequest(
            @NotBlank(message = "用户名不能为空")
            @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{2,31}$", message = "用户名需为3-32位字母、数字或下划线，且以字母开头") String username,
            @NotBlank(message = "显示名称不能为空") @Size(max = 64, message = "显示名称不能超过64位") String displayName,
            @NotBlank(message = "密码不能为空") @Size(min = 8, max = 128, message = "密码长度应为8-128位") String password,
            @NotNull(message = "至少需要指定一个角色") Set<String> roleCodes) { }

    public record UpdateUserStatusRequest(@NotNull(message = "启用状态不能为空") Boolean enabled) { }
}
