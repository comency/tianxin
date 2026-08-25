package com.tianxin.platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tianxin.platform.security.JwtTokenService;
import com.tianxin.platform.security.PasswordHasher;
import com.tianxin.platform.system.store.InMemoryIdentityStore;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    @Test
    void administratorCanLoginAndItsTokenCanBeVerified() {
        PasswordHasher passwordHasher = new PasswordHasher();
        JwtTokenService tokenService = new JwtTokenService("test-secret-for-jwt-signing", 3600);
        AuthService authService = new AuthService(new InMemoryIdentityStore(passwordHasher), passwordHasher, tokenService);

        AuthService.LoginResult result = authService.login("admin", "admin123");

        assertThat(result.user().username()).isEqualTo("admin");
        assertThat(tokenService.verifyAndGetSubject(result.accessToken())).isPresent();
    }

    @Test
    void invalidPasswordCannotLogin() {
        PasswordHasher passwordHasher = new PasswordHasher();
        AuthService authService = new AuthService(new InMemoryIdentityStore(passwordHasher), passwordHasher,
                new JwtTokenService("test-secret-for-jwt-signing", 3600));

        assertThatThrownBy(() -> authService.login("admin", "wrong-password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("用户名或密码错误");
    }
}
