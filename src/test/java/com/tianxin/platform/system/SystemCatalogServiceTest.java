package com.tianxin.platform.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.tianxin.platform.system.store.SystemCatalogStore;
import org.junit.jupiter.api.Test;

class SystemCatalogServiceTest {

    @Test
    void systemMenuContainsManagementChildren() {
        SystemCatalogService service = new SystemCatalogService(new SystemCatalogStore());

        var tree = service.menuTree();

        assertThat(tree).extracting(SystemCatalogService.MenuNode::name).contains("首页", "系统管理");
        assertThat(tree.stream().filter(node -> node.name().equals("系统管理")).findFirst().orElseThrow().children())
                .extracting(SystemCatalogService.MenuNode::name)
                .contains("用户管理", "数据字典", "操作日志");
    }
}
