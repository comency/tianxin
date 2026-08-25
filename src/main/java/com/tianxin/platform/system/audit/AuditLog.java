package com.tianxin.platform.system.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditLog(UUID id, Instant occurredAt, UUID operatorId, String method, String path, boolean success,
                       long durationMs) {
}
