package com.tianxin.platform.system.model;

import java.util.Set;

public record SystemRole(String code, String name, Set<String> permissions) {
}
