package com.tianxin.platform.system.store;

import com.tianxin.platform.system.model.Department;
import com.tianxin.platform.system.model.DictionaryItem;
import com.tianxin.platform.system.model.SystemMenu;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Seeded platform-management catalog for the first development iteration. */
@Repository
public class SystemCatalogStore {

    private static final UUID ROOT_MENU = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SYSTEM_MENU = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ROOT_DEPARTMENT = UUID.fromString("10000000-0000-0000-0000-000000000001");

    private final List<SystemMenu> menus = List.of(
            new SystemMenu(ROOT_MENU, null, "首页", "/dashboard", "dashboard", null, 1, true),
            new SystemMenu(SYSTEM_MENU, null, "系统管理", "/system", "setting", null, 99, true),
            new SystemMenu(UUID.fromString("00000000-0000-0000-0000-000000000003"), SYSTEM_MENU,
                    "用户管理", "/system/users", "user", "sys:user:read", 1, true),
            new SystemMenu(UUID.fromString("00000000-0000-0000-0000-000000000004"), SYSTEM_MENU,
                    "角色管理", "/system/roles", "role", "sys:role:read", 2, true),
            new SystemMenu(UUID.fromString("00000000-0000-0000-0000-000000000005"), SYSTEM_MENU,
                    "部门管理", "/system/departments", "organization", "sys:dept:read", 3, true),
            new SystemMenu(UUID.fromString("00000000-0000-0000-0000-000000000006"), SYSTEM_MENU,
                    "数据字典", "/system/dictionaries", "dict", "sys:dict:read", 4, true),
            new SystemMenu(UUID.fromString("00000000-0000-0000-0000-000000000007"), SYSTEM_MENU,
                    "操作日志", "/system/audit-logs", "log", "sys:audit:read", 5, true));

    private final List<Department> departments = List.of(
            new Department(ROOT_DEPARTMENT, null, "天信管业科技集团", "平台管理员", "", 1, true),
            new Department(UUID.fromString("10000000-0000-0000-0000-000000000002"), ROOT_DEPARTMENT,
                    "运营中心", "运营负责人", "", 1, true),
            new Department(UUID.fromString("10000000-0000-0000-0000-000000000003"), ROOT_DEPARTMENT,
                    "技术中心", "技术负责人", "", 2, true));

    private final List<DictionaryItem> dictionaryItems = List.of(
            new DictionaryItem("user_status", "enabled", "正常", "1", 1, true),
            new DictionaryItem("user_status", "disabled", "停用", "0", 2, true),
            new DictionaryItem("enterprise_type", "manufacturer", "生产制造企业", "MANUFACTURER", 1, true),
            new DictionaryItem("enterprise_type", "construction", "施工企业", "CONSTRUCTION", 2, true),
            new DictionaryItem("enterprise_type", "supplier", "材料供应商", "SUPPLIER", 3, true));

    public List<SystemMenu> listMenus() {
        return menus.stream().filter(SystemMenu::visible).sorted(Comparator.comparingInt(SystemMenu::sortOrder)).toList();
    }

    public List<Department> listDepartments() {
        return departments.stream().filter(Department::enabled).sorted(Comparator.comparingInt(Department::sortOrder)).toList();
    }

    public List<DictionaryItem> listDictionaryItems(String typeCode) {
        return dictionaryItems.stream()
                .filter(DictionaryItem::enabled)
                .filter(item -> typeCode == null || typeCode.isBlank() || item.typeCode().equals(typeCode))
                .sorted(Comparator.comparing(DictionaryItem::typeCode).thenComparingInt(DictionaryItem::sortOrder))
                .toList();
    }
}
