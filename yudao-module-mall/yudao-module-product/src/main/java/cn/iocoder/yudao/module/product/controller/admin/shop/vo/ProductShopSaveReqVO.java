package cn.iocoder.yudao.module.product.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 企业店铺新增/更新 Request VO")
@Data
public class ProductShopSaveReqVO {

    @Schema(description = "店铺编号", example = "1")
    private Long id;

    @Schema(description = "企业名录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "企业名录编号不能为空")
    private Long enterpriseId;

    @Schema(description = "店铺名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "天信管业旗舰店")
    @NotBlank(message = "店铺名称不能为空")
    private String name;

    @Schema(description = "店铺 Logo")
    private String logoUrl;

    @Schema(description = "联系人", example = "张三")
    private String contactName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactMobile;

    @Schema(description = "店铺负责人（后台系统用户编号）", example = "1")
    private Long managerUserId;

    @Schema(description = "店铺简介")
    private String introduction;

    @Schema(description = "状态（0正常 1停用）", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;
}
