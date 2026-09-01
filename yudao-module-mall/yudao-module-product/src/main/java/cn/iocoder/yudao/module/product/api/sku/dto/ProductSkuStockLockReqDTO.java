package cn.iocoder.yudao.module.product.api.sku.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商城订单库存履约请求。
 *
 * <p>订单号是库存预占、释放和出库的幂等键，不能使用订单数据库主键代替。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuStockLockReqDTO {

    @NotEmpty(message = "订单号不能为空")
    private String orderNo;

    @NotEmpty(message = "商品 SKU 不能为空")
    private List<Item> items;

    @Data
    public static class Item {

        @NotNull(message = "商品 SKU 编号不能为空")
        private Long id;

        @NotNull(message = "商品数量不能为空")
        private Integer count;
    }
}
