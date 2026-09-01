package cn.iocoder.yudao.module.trade.controller.admin.settlement.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class ShopSettlementGenerateReqVO {

    @NotNull(message = "店铺不能为空")
    private Long shopId;
    @NotNull(message = "结算周期开始时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime periodStartTime;
    @NotNull(message = "结算周期结束时间不能为空")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime periodEndTime;
    /** 万分比；500 表示 5%。 */
    @NotNull(message = "平台佣金比例不能为空")
    @Min(value = 0, message = "平台佣金比例不能小于 0")
    @Max(value = 10000, message = "平台佣金比例不能大于 100%")
    private Integer commissionRate;
}
