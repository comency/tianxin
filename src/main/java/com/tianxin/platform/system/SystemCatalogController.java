package com.tianxin.platform.system;

import com.tianxin.platform.common.api.ApiResponse;
import com.tianxin.platform.system.model.DictionaryItem;
import com.tianxin.platform.system.store.SystemCatalogStore;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class SystemCatalogController {

    private final SystemCatalogService catalogService;
    private final SystemCatalogStore catalogStore;

    public SystemCatalogController(SystemCatalogService catalogService, SystemCatalogStore catalogStore) {
        this.catalogService = catalogService;
        this.catalogStore = catalogStore;
    }

    @GetMapping("/menus/tree")
    public ApiResponse<List<SystemCatalogService.MenuNode>> menuTree() {
        return ApiResponse.success(catalogService.menuTree());
    }

    @GetMapping("/departments/tree")
    public ApiResponse<List<SystemCatalogService.DepartmentNode>> departmentTree() {
        return ApiResponse.success(catalogService.departmentTree());
    }

    @GetMapping("/dictionaries")
    public ApiResponse<List<DictionaryItem>> dictionaries(@RequestParam(required = false) String typeCode) {
        return ApiResponse.success(catalogStore.listDictionaryItems(typeCode));
    }
}
