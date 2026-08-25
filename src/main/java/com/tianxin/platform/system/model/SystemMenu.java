package com.tianxin.platform.system.model;

import java.util.UUID;

public record SystemMenu(UUID id, UUID parentId, String name, String route, String icon, String permission,
                         int sortOrder, boolean visible) {
}
