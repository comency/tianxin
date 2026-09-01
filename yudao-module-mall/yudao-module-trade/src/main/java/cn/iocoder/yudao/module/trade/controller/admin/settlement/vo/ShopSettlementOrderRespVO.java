package cn.iocoder.yudao.module.trade.controller.admin.settlement.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShopSettlementOrderRespVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private LocalDateTime orderFinishTime;
    private Integer payAmount;
    private Integer refundAmount;
    private Integer settlementBaseAmount;
    private Integer platformCommissionAmount;
    private Integer settlementAmount;
}
