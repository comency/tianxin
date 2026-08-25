package com.tianxin.platform.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private static final String HEADER = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private final byte[] secret;
    private final long ttlSeconds;

    public JwtTokenService(
            @Value("${tx.security.jwt.secret}") String secret,
            @Value("${tx.security.jwt.ttl-seconds}") long ttlSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
    }

    public IssuedToken issue(String userId) {
        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);
        String payload = "{\"sub\":\"" + userId + "\",\"exp\":" + expiresAt.getEpochSecond() + "}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String unsigned = HEADER + "." + encodedPayload;
        return new IssuedToken(unsigned + "." + signature(unsigned), expiresAt);
    }

    public Optional<String> verifyAndGetSubject(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3 || !HEADER.equals(parts[0])) {
            return Optional.empty();
        }
        String unsigned = parts[0] + "." + parts[1];
        if (!MessageDigest.isEqual(signature(unsigned).getBytes(StandardCharsets.US_ASCII),
                parts[2].getBytes(StandardCharsets.US_ASCII))) {
            return Optional.empty();
        }
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            int subjectStart = payload.indexOf("\"sub\":\"") + 7;
            int subjectEnd = payload.indexOf('"', subjectStart);
            int expirationStart = payload.indexOf("\"exp\":") + 6;
            int expirationEnd = payload.indexOf('}', expirationStart);
            if (subjectStart < 7 || subjectEnd < subjectStart || expirationStart < 6 || expirationEnd < expirationStart) {
                return Optional.empty();
            }
            long expiration = Long.parseLong(payload.substring(expirationStart, expirationEnd));
            if (Instant.now().getEpochSecond() >= expiration) {
                return Optional.empty();
            }
            return Optional.of(payload.substring(subjectStart, subjectEnd));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String signature(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("无法初始化令牌签名器", exception);
        }
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
