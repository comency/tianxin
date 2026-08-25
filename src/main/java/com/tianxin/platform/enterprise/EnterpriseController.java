package com.tianxin.platform.enterprise;

import com.tianxin.platform.common.api.ApiResponse;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("mysql")
@RequestMapping("/api/v1/enterprises")
public class EnterpriseController {

    private final EnterpriseRepository enterpriseRepository;

    public EnterpriseController(EnterpriseRepository enterpriseRepository) {
        this.enterpriseRepository = enterpriseRepository;
    }

    @GetMapping
    public ApiResponse<List<Enterprise>> list(@RequestParam(required = false) EnterpriseCategory category) {
        return ApiResponse.success(enterpriseRepository.list(category));
    }

    @GetMapping("/categories")
    public ApiResponse<List<EnterpriseCategorySummary>> categories() {
        return ApiResponse.success(enterpriseRepository.summarizeByCategory());
    }
}
