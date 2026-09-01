package cn.iocoder.yudao.module.statistics.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 店铺运营统计 Response VO")
@Data
public class ShopOperationStatisticsRespVO {

    @Schema(description = "店铺编号")
    private Long shopId;
    @Schema(description = "店铺名称")
    private String shopName;
    @Schema(description = "订单数")
    private Integer orderCount;
    @Schema(description = "已支付订单数")
    private Integer paidOrderCount;
    @Schema(description = "支付金额，单位：分")
    private Integer paidAmount;
    @Schema(description = "退款订单数")
    private Integer refundOrderCount;
    @Schema(description = "退款金额，单位：分")
    private Integer refundAmount;
    @Schema(description = "已结算金额，单位：分")
    private Integer settledAmount;
    @Schema(description = "待结算金额，单位：分")
    private Integer unsettledAmount;
}
