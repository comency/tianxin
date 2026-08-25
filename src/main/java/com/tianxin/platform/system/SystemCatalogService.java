package com.tianxin.platform.system;

import com.tianxin.platform.system.model.Department;
import com.tianxin.platform.system.model.SystemMenu;
import com.tianxin.platform.system.store.SystemCatalogStore;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SystemCatalogService {

    private final SystemCatalogStore catalogStore;

    public SystemCatalogService(SystemCatalogStore catalogStore) {
        this.catalogStore = catalogStore;
    }

    public List<MenuNode> menuTree() {
        return catalogStore.listMenus().stream().filter(menu -> menu.parentId() == null)
                .map(menu -> mapMenu(menu, catalogStore.listMenus())).toList();
    }

    public List<DepartmentNode> departmentTree() {
        return catalogStore.listDepartments().stream().filter(department -> department.parentId() == null)
                .map(department -> mapDepartment(department, catalogStore.listDepartments())).toList();
    }

    private MenuNode mapMenu(SystemMenu menu, List<SystemMenu> allMenus) {
        List<MenuNode> children = allMenus.stream().filter(item -> menu.id().equals(item.parentId()))
                .sorted(Comparator.comparingInt(SystemMenu::sortOrder)).map(item -> mapMenu(item, allMenus)).toList();
        return new MenuNode(menu.id(), menu.name(), menu.route(), menu.icon(), menu.permission(), children);
    }

    private DepartmentNode mapDepartment(Department department, List<Department> allDepartments) {
        List<DepartmentNode> children = allDepartments.stream().filter(item -> department.id().equals(item.parentId()))
                .sorted(Comparator.comparingInt(Department::sortOrder))
                .map(item -> mapDepartment(item, allDepartments)).toList();
        return new DepartmentNode(department.id(), department.name(), department.leader(), department.phone(), children);
    }

    public record MenuNode(UUID id, String name, String route, String icon, String permission, List<MenuNode> children) { }
    public record DepartmentNode(UUID id, String name, String leader, String phone, List<DepartmentNode> children) { }
}
