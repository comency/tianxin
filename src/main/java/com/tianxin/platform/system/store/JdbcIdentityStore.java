package com.tianxin.platform.system.store;

import com.tianxin.platform.security.PasswordHasher;
import com.tianxin.platform.system.model.SystemRole;
import com.tianxin.platform.system.model.SystemUser;
import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** MySQL implementation used when the mysql Spring profile is active. */
@Repository
@Profile("mysql")
public class JdbcIdentityStore implements IdentityStore {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordHasher passwordHasher;

    public JdbcIdentityStore(JdbcTemplate jdbcTemplate, PasswordHasher passwordHasher) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordHasher = passwordHasher;
    }

    @PostConstruct
    @Transactional
    void initializeSeedData() {
        jdbcTemplate.update("INSERT INTO sys_role(role_code, name, permissions) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE name = VALUES(name), permissions = VALUES(permissions)",
                "ADMIN", "平台管理员", "*:*:*");
        jdbcTemplate.update("INSERT INTO sys_role(role_code, name, permissions) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE name = VALUES(name), permissions = VALUES(permissions)",
                "EDITOR", "内容编辑", "content:article:read,content:article:write");
        jdbcTemplate.update("INSERT INTO sys_role(role_code, name, permissions) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE name = VALUES(name), permissions = VALUES(permissions)",
                "VIEWER", "只读用户", "content:article:read");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
        if (count != null && count == 0) {
            UUID id = UUID.randomUUID();
            jdbcTemplate.update("INSERT INTO sys_user(id, username, display_name, password_hash, enabled, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    id.toString(), "admin", "平台管理员", passwordHasher.hash("admin123"), true,
                    Timestamp.from(Instant.now()));
            jdbcTemplate.update("INSERT INTO sys_user_role(user_id, role_code) VALUES (?, ?)", id.toString(), "ADMIN");
        }
    }

    @Override
    public Optional<SystemUser> findUserByUsername(String username) {
        return jdbcTemplate.query("SELECT * FROM sys_user WHERE username = ?", this::mapUser, username).stream().findFirst();
    }

    @Override
    public Optional<SystemUser> findUserById(UUID id) {
        return jdbcTemplate.query("SELECT * FROM sys_user WHERE id = ?", this::mapUser, id.toString()).stream().findFirst();
    }

    @Override
    public List<SystemUser> listUsers() {
        return jdbcTemplate.query("SELECT * FROM sys_user ORDER BY created_at", this::mapUser);
    }

    @Override
    public List<SystemRole> listRoles() {
        return jdbcTemplate.query("SELECT role_code, name, permissions FROM sys_role ORDER BY role_code",
                (resultSet, rowNumber) -> new SystemRole(resultSet.getString("role_code"), resultSet.getString("name"),
                        splitPermissions(resultSet.getString("permissions"))));
    }

    @Override
    @Transactional
    public SystemUser createUser(String username, String displayName, String password, Set<String> roleCodes) {
        if (findUserByUsername(username).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        Set<String> knownRoles = listRoles().stream().map(SystemRole::code).collect(java.util.stream.Collectors.toSet());
        if (roleCodes.isEmpty() || !knownRoles.containsAll(roleCodes)) {
            throw new IllegalArgumentException("角色编码无效");
        }
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        jdbcTemplate.update("INSERT INTO sys_user(id, username, display_name, password_hash, enabled, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                id.toString(), username, displayName, passwordHasher.hash(password), true, Timestamp.from(createdAt));
        for (String roleCode : roleCodes) {
            jdbcTemplate.update("INSERT INTO sys_user_role(user_id, role_code) VALUES (?, ?)", id.toString(), roleCode);
        }
        return new SystemUser(id, username, displayName, findUserById(id).orElseThrow().passwordHash(), roleCodes, true, createdAt);
    }

    @Override
    public void updateUserStatus(UUID id, boolean enabled) {
        if (jdbcTemplate.update("UPDATE sys_user SET enabled = ? WHERE id = ?", enabled, id.toString()) == 0) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private SystemUser mapUser(ResultSet resultSet, int rowNumber) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        List<String> roleCodes = jdbcTemplate.queryForList("SELECT role_code FROM sys_user_role WHERE user_id = ? ORDER BY role_code",
                String.class, id.toString());
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new SystemUser(id, resultSet.getString("username"), resultSet.getString("display_name"),
                resultSet.getString("password_hash"), new LinkedHashSet<>(roleCodes), resultSet.getBoolean("enabled"),
                createdAt.toInstant());
    }

    private Set<String> splitPermissions(String permissions) {
        if (permissions == null || permissions.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(permissions.split(",")).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
