package cn.iocoder.yudao.module.product.api.sku.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 商城订单在 WMS 中的库存履约状态。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuStockStatusRespDTO {

    private String status;
    private Integer totalCount;
    private Integer lockedCount;
    private Integer releasedCount;
    private Integer outboundCount;

}
