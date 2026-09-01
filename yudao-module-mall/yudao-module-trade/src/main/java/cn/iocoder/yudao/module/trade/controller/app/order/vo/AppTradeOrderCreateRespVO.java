package cn.iocoder.yudao.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 交易订单创建 Response VO")
@Data
public class AppTradeOrderCreateRespVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "支付订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long payOrderId;

    @Schema(description = "本次创建的订单编号列表；多店铺结算时会返回多笔订单", example = "[1024, 1025]")
    private List<Long> orderIds;

    @Schema(description = "本次创建的支付单编号列表；多店铺订单需分别支付", example = "[2048, 2049]")
    private List<Long> payOrderIds;

}
