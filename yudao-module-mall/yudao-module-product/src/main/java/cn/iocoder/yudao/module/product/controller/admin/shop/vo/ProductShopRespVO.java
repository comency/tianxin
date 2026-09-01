package cn.iocoder.yudao.module.product.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 企业店铺 Response VO")
@Data
public class ProductShopRespVO {

    private Long id;
    private Long enterpriseId;
    private String name;
    private String logoUrl;
    private String contactName;
    private String contactMobile;
    private Long managerUserId;
    private String introduction;
    private Integer status;
    private LocalDateTime createTime;
}
