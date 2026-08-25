package com.tianxin.platform.system.model;

import java.util.UUID;

public record Department(UUID id, UUID parentId, String name, String leader, String phone, int sortOrder,
                         boolean enabled) {
}
