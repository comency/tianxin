package cn.iocoder.yudao.module.trade.controller.admin.settlement.vo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShopSettlementAuditReqVO {

    @NotNull(message = "结算单编号不能为空")
    private Long id;
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    @Size(max = 512, message = "审核备注不能超过 512 个字符")
    private String auditRemark;
}
