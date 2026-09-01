package cn.iocoder.yudao.module.product.service.sku.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/** 商城商品库存与 WMS 库存的单条对账结果。 */
@Data
@AllArgsConstructor
public class ProductWmsStockReconciliationDTO {

    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_CACHE_DIFFERENCE = "CACHE_DIFFERENCE";
    public static final String STATUS_MISSING_INVENTORY = "MISSING_INVENTORY";

    private Long productSkuId;
    private Long spuId;
    private String spuName;
    private Long wmsSkuId;
    private Long wmsWarehouseId;
    private Integer cachedStock;
    private BigDecimal physicalQuantity;
    private BigDecimal lockedQuantity;
    private Integer availableStock;
    private String status;

}
