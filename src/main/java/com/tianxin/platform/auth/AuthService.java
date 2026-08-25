package com.tianxin.platform.auth;

import com.tianxin.platform.security.JwtTokenService;
import com.tianxin.platform.security.PasswordHasher;
import com.tianxin.platform.system.model.SystemRole;
import com.tianxin.platform.system.model.SystemUser;
import com.tianxin.platform.system.store.IdentityStore;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final IdentityStore identityStore;
    private final PasswordHasher passwordHasher;
    private final JwtTokenService tokenService;

    public AuthService(IdentityStore identityStore, PasswordHasher passwordHasher, JwtTokenService tokenService) {
        this.identityStore = identityStore;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    public LoginResult login(String username, String password) {
        SystemUser user = identityStore.findUserByUsername(username)
                .filter(SystemUser::enabled)
                .filter(candidate -> passwordHasher.matches(password, candidate.passwordHash()))
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        JwtTokenService.IssuedToken token = tokenService.issue(user.id().toString());
        return new LoginResult(token.value(), token.expiresAt(), toUserView(user));
    }

    public SystemUser requireUser(UUID userId) {
        return identityStore.findUserById(userId)
                .filter(SystemUser::enabled)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在或已停用"));
    }

    public boolean hasPermission(SystemUser user, String requiredPermission) {
        return user.roleCodes().stream()
                .map(roleCode -> identityStore.listRoles().stream()
                        .filter(role -> role.code().equals(roleCode))
                        .findFirst()
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(SystemRole::permissions)
                .anyMatch(permissions -> permissions.contains("*:*:*") || permissions.contains(requiredPermission));
    }

    public UserView toUserView(SystemUser user) {
        List<String> roles = user.roleCodes().stream().sorted().toList();
        return new UserView(user.id(), user.username(), user.displayName(), roles, user.enabled(), user.createdAt());
    }

    public record LoginResult(String accessToken, java.time.Instant expiresAt, UserView user) { }
    public record UserView(UUID id, String username, String displayName, List<String> roles, boolean enabled,
                           java.time.Instant createdAt) { }
}
