package com.tianxin.platform.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.tianxin.platform.common.api.ApiResponse;
import org.junit.jupiter.api.Test;

class PlatformControllerTest {

    @Test
    void overviewContainsTheFirstPhaseModules() {
        var service = new PlatformService("tianxin-smart-platform", "0.1.0-SNAPSHOT");

        ApiResponse<java.util.Map<String, Object>> response = ApiResponse.success(service.overview());

        assertThat(response.success()).isTrue();
        assertThat(response.data()).containsEntry("status", "RUNNING");
        assertThat(response.data().get("modules"))
                .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST)
                .contains("系统管理", "AI咨询");
    }
}
