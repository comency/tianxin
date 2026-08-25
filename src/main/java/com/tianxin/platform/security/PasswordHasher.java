package com.tianxin.platform.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(salt) + "$"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(derive(password.toCharArray(), salt));
    }

    public boolean matches(String password, String encodedValue) {
        String[] parts = encodedValue.split("\\$", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getUrlDecoder().decode(parts[0]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[1]);
            return java.security.MessageDigest.isEqual(expected, derive(password.toCharArray(), salt));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] derive(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("无法初始化密码加密器", exception);
        }
    }
}
