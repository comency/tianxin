package com.tianxin.platform.system.store;

import com.tianxin.platform.security.PasswordHasher;
import com.tianxin.platform.system.model.SystemRole;
import com.tianxin.platform.system.model.SystemUser;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

/** Temporary development store. Replace with a MySQL-backed implementation before production. */
@Repository
@Profile("!mysql")
public class InMemoryIdentityStore implements IdentityStore {

    private final PasswordHasher passwordHasher;
    private final Map<UUID, SystemUser> users = new LinkedHashMap<>();
    private final Map<String, SystemRole> roles = Map.of(
            "ADMIN", new SystemRole("ADMIN", "平台管理员", Set.of("*:*:*")),
            "EDITOR", new SystemRole("EDITOR", "内容编辑", Set.of("content:article:read", "content:article:write")),
            "VIEWER", new SystemRole("VIEWER", "只读用户", Set.of("content:article:read")));

    public InMemoryIdentityStore(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
        SystemUser administrator = new SystemUser(UUID.randomUUID(), "admin", "平台管理员",
                passwordHasher.hash("admin123"), Set.of("ADMIN"), true, Instant.now());
        users.put(administrator.id(), administrator);
    }

    @Override
    public synchronized Optional<SystemUser> findUserByUsername(String username) {
        return users.values().stream().filter(user -> user.username().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public synchronized Optional<SystemUser> findUserById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public synchronized List<SystemUser> listUsers() {
        return users.values().stream().sorted(Comparator.comparing(SystemUser::createdAt)).toList();
    }

    @Override
    public List<SystemRole> listRoles() {
        return roles.values().stream().sorted(Comparator.comparing(SystemRole::code)).toList();
    }

    @Override
    public synchronized SystemUser createUser(String username, String displayName, String password, Set<String> roleCodes) {
        if (findUserByUsername(username).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (roleCodes.isEmpty() || !roles.keySet().containsAll(roleCodes)) {
            throw new IllegalArgumentException("角色编码无效");
        }
        SystemUser user = new SystemUser(UUID.randomUUID(), username, displayName, passwordHasher.hash(password),
                roleCodes, true, Instant.now());
        users.put(user.id(), user);
        return user;
    }

    @Override
    public synchronized void updateUserStatus(UUID id, boolean enabled) {
        SystemUser user = findUserById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEnabled(enabled);
    }
}
