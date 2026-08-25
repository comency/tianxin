package com.tianxin.platform.system;

import com.tianxin.platform.common.api.ApiResponse;
import com.tianxin.platform.system.model.SystemRole;
import com.tianxin.platform.system.store.IdentityStore;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/roles")
public class SystemRoleController {

    private final IdentityStore identityStore;

    public SystemRoleController(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    @GetMapping
    public ApiResponse<List<SystemRole>> list() {
        return ApiResponse.success(identityStore.listRoles());
    }
}
