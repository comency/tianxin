package cn.iocoder.yudao.module.product.api.sku;

import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuRespDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuStockLockReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuStockStatusRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 商品 SKU API 接口
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
public interface ProductSkuApi {

    /**
     * 查询 SKU 信息
     *
     * @param id SKU 编号
     * @return SKU 信息
     */
    ProductSkuRespDTO getSku(Long id);

    /**
     * 批量查询 SKU 数组
     *
     * @param ids SKU 编号列表
     * @return SKU 数组
     */
    List<ProductSkuRespDTO> getSkuList(Collection<Long> ids);

    /**
     * 批量查询 SKU MAP
     *
     * @param ids SKU 编号列表
     * @return SKU MAP
     */
    default Map<Long, ProductSkuRespDTO> getSkuMap(Collection<Long> ids) {
        return convertMap(getSkuList(ids), ProductSkuRespDTO::getId);
    }

    /**
     * 批量查询 SKU 数组
     *
     * @param spuIds SPU 编号列表
     * @return SKU 数组
     */
    List<ProductSkuRespDTO> getSkuListBySpuId(Collection<Long> spuIds);

    /**
     * 更新 SKU 库存（增加 or 减少）
     *
     * @param updateStockReqDTO 更新请求
     */
    void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO);

    void reserveSkuStock(ProductSkuStockLockReqDTO reqDTO);

    void releaseSkuStock(ProductSkuStockLockReqDTO reqDTO);

    void outboundSkuStock(ProductSkuStockLockReqDTO reqDTO);

    void inboundReturnSkuStock(String returnNo, ProductSkuStockLockReqDTO reqDTO);

    /** 批量查询订单的 WMS 库存履约状态，键为商城订单号。 */
    Map<String, ProductSkuStockStatusRespDTO> getWmsStockStatus(Collection<String> orderNos);

}
