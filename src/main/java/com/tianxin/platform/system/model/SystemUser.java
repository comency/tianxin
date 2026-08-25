package com.tianxin.platform.system.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class SystemUser {

    private final UUID id;
    private final String username;
    private final String displayName;
    private final String passwordHash;
    private final Set<String> roleCodes;
    private final Instant createdAt;
    private boolean enabled;

    public SystemUser(UUID id, String username, String displayName, String passwordHash, Set<String> roleCodes,
            boolean enabled, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.roleCodes = new LinkedHashSet<>(roleCodes);
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public UUID id() { return id; }
    public String username() { return username; }
    public String displayName() { return displayName; }
    public String passwordHash() { return passwordHash; }
    public Set<String> roleCodes() { return Set.copyOf(roleCodes); }
    public boolean enabled() { return enabled; }
    public Instant createdAt() { return createdAt; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
