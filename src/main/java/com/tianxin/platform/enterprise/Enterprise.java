package com.tianxin.platform.enterprise;

import java.util.UUID;

public record Enterprise(UUID id, String companyCode, String companyName, EnterpriseCategory category,
                         String categoryName, String businessScope, int sortOrder, boolean enabled) {
}
