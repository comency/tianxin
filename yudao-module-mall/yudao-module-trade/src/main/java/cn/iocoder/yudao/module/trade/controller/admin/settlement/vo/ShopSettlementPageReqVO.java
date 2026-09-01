package cn.iocoder.yudao.module.trade.controller.admin.settlement.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.module.trade.enums.settlement.ShopSettlementStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 店铺结算单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopSettlementPageReqVO extends PageParam {

    private String no;
    private Long shopId;
    /** {@link ShopSettlementStatusEnum} */
    private Integer status;
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
