package cn.iocoder.yudao.module.statistics.controller.admin.shop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 店铺运营统计 Request VO")
@Data
public class ShopOperationStatisticsReqVO {

    @Schema(description = "店铺编号，为空时统计全部店铺")
    private Long shopId;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "统计时间范围")
    private LocalDateTime[] times;
}
