package com.tianxin.platform.system.audit;

import com.tianxin.platform.common.api.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/audit-logs")
public class AuditLogController {

    private final AuditLogStore auditLogStore;

    public AuditLogController(AuditLogStore auditLogStore) {
        this.auditLogStore = auditLogStore;
    }

    @GetMapping
    public ApiResponse<List<AuditLog>> list() {
        return ApiResponse.success(auditLogStore.list());
    }
}
