package cn.iocoder.yudao.module.product.service.sku;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuUpdateStockReqDTO;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuStockLockReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.ProductSkuSaveReqVO;
import cn.iocoder.yudao.module.product.convert.sku.ProductSkuConvert;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyDO;
import cn.iocoder.yudao.module.product.dal.dataobject.property.ProductPropertyValueDO;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.ProductSkuDO;
import cn.iocoder.yudao.module.product.dal.mysql.sku.ProductSkuMapper;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyService;
import cn.iocoder.yudao.module.product.service.property.ProductPropertyValueService;
import cn.iocoder.yudao.module.product.service.spu.ProductSpuService;
import cn.iocoder.yudao.module.product.service.sku.adapter.ProductWmsInventoryAdapter;
import cn.iocoder.yudao.module.product.service.sku.dto.ProductWmsStockReconciliationDTO;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 商品 SKU Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ProductSkuServiceImpl implements ProductSkuService {

    @Resource
    private ProductSkuMapper productSkuMapper;

    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductSpuService productSpuService;
    @Resource
    @Lazy // 循环依赖，避免报错
    private ProductPropertyService productPropertyService;
    @Resource
    private ProductPropertyValueService productPropertyValueService;
    @Resource
    private ProductWmsInventoryAdapter productWmsInventoryAdapter;

    @Override
    public void deleteSku(Long id) {
        // 校验存在
        validateSkuExists(id);
        // 删除
        productSkuMapper.deleteById(id);
    }

    private void validateSkuExists(Long id) {
        if (productSkuMapper.selectById(id) == null) {
            throw exception(SKU_NOT_EXISTS);
        }
    }

    @Override
    public ProductSkuDO getSku(Long id) {
        return refreshWmsStock(productSkuMapper.selectById(id));
    }

    @Override
    public ProductSkuDO getSku(Long id, boolean includeDeleted) {
        if (includeDeleted) {
            return refreshWmsStock(productSkuMapper.selectByIdIncludeDeleted(id));
        }
        return getSku(id);
    }

    @Override
    public List<ProductSkuDO> getSkuList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return refreshWmsStock(productSkuMapper.selectByIds(ids));
    }

    @Override
    public void validateSkuList(List<ProductSkuSaveReqVO> skus, Boolean specType) {
        // 0、校验skus是否为空
        if (CollUtil.isEmpty(skus)) {
            throw exception(SKU_NOT_EXISTS);
        }
        skus.forEach(this::validateWmsMapping);
        // 单规格，赋予单规格默认属性
        if (ObjectUtil.equal(specType, false)) {
            ProductSkuSaveReqVO skuVO = skus.get(0);
            List<ProductSkuSaveReqVO.Property> properties = new ArrayList<>();
            ProductSkuSaveReqVO.Property property = new ProductSkuSaveReqVO.Property()
                    .setPropertyId(ProductPropertyDO.ID_DEFAULT).setPropertyName(ProductPropertyDO.NAME_DEFAULT)
                    .setValueId(ProductPropertyValueDO.ID_DEFAULT).setValueName(ProductPropertyValueDO.NAME_DEFAULT);
            properties.add(property);
            skuVO.setProperties(properties);
            return; // 单规格不需要后续的校验
        }

        // 1、校验属性项存在
        Set<Long> propertyIds = skus.stream().filter(p -> p.getProperties() != null)
                // 遍历多个 Property 属性
                .flatMap(p -> p.getProperties().stream())
                // 将每个 Property 转换成对应的 propertyId，最后形成集合
                .map(ProductSkuSaveReqVO.Property::getPropertyId)
                .collect(Collectors.toSet());
        List<ProductPropertyDO> propertyList = productPropertyService.getPropertyList(propertyIds);
        if (propertyList.size() != propertyIds.size()) {
            throw exception(PROPERTY_NOT_EXISTS);
        }

        // 2. 校验，一个 SKU 下，没有重复的属性。校验方式是，遍历每个 SKU ，看看是否有重复的属性 propertyId
        Map<Long, ProductPropertyValueDO> propertyValueMap = convertMap(productPropertyValueService.getPropertyValueListByPropertyId(propertyIds), ProductPropertyValueDO::getId);
        skus.forEach(sku -> {
            Set<Long> skuPropertyIds = convertSet(sku.getProperties(), propertyItem -> propertyValueMap.get(propertyItem.getValueId()).getPropertyId());
            if (skuPropertyIds.size() != sku.getProperties().size()) {
                throw exception(SKU_PROPERTIES_DUPLICATED);
            }
        });

        // 3. 再校验，每个 Sku 的属性值的数量，是一致的。
        int attrValueIdsSize = skus.get(0).getProperties().size();
        for (int i = 1; i < skus.size(); i++) {
            if (attrValueIdsSize != skus.get(i).getProperties().size()) {
                throw exception(SPU_ATTR_NUMBERS_MUST_BE_EQUALS);
            }
        }

        // 4. 最后校验，每个 Sku 之间不是重复的
        // 每个元素，都是一个 Sku 的 attrValueId 集合。这样，通过最外层的 Set ，判断是否有重复的.
        Set<Set<Long>> skuAttrValues = new HashSet<>();
        for (ProductSkuSaveReqVO sku : skus) {
            // 添加失败，说明重复
            if (!skuAttrValues.add(convertSet(sku.getProperties(), ProductSkuSaveReqVO.Property::getValueId))) {
                throw exception(SPU_SKU_NOT_DUPLICATE);
            }
        }
    }

    @Override
    public void createSkuList(Long spuId, List<ProductSkuSaveReqVO> skuCreateReqList) {
        List<ProductSkuDO> skus = BeanUtils.toBean(skuCreateReqList, ProductSkuDO.class, sku -> sku.setSpuId(spuId).setSalesCount(0));
        productSkuMapper.insertBatch(skus);
    }

    @Override
    public List<ProductSkuDO> getSkuListBySpuId(Long spuId) {
        return refreshWmsStock(productSkuMapper.selectListBySpuId(spuId));
    }

    @Override
    public List<ProductSkuDO> getSkuListBySpuId(Collection<Long> spuIds) {
        if (CollUtil.isEmpty(spuIds)) {
            return Collections.emptyList();
        }
        return refreshWmsStock(productSkuMapper.selectListBySpuId(spuIds));
    }

    @Override
    public void deleteSkuBySpuId(Long spuId) {
        productSkuMapper.deleteBySpuId(spuId);
    }

    @Override
    public int updateSkuProperty(Long propertyId, String propertyName) {
        // 获取所有的 sku
        List<ProductSkuDO> skuDOList = productSkuMapper.selectList();
        // 处理后需要更新的 sku
        List<ProductSkuDO> updateSkus = new ArrayList<>();
        if (CollUtil.isEmpty(skuDOList)) {
            return 0;
        }
        skuDOList.stream().filter(sku -> sku.getProperties() != null)
                .forEach(sku -> sku.getProperties().forEach(property -> {
                    if (property.getPropertyId().equals(propertyId)) {
                        property.setPropertyName(propertyName);
                        updateSkus.add(sku);
                    }
                }));
        if (CollUtil.isEmpty(updateSkus)) {
            return 0;
        }

        productSkuMapper.updateBatch(updateSkus);
        return updateSkus.size();
    }

    @Override
    public int updateSkuPropertyValue(Long propertyValueId, String propertyValueName) {
        // 获取所有的 sku
        List<ProductSkuDO> skuDOList = productSkuMapper.selectList();
        // 处理后需要更新的 sku
        List<ProductSkuDO> updateSkus = new ArrayList<>();
        if (CollUtil.isEmpty(skuDOList)) {
            return 0;
        }
        skuDOList.stream()
                .filter(sku -> sku.getProperties() != null)
                .forEach(sku -> sku.getProperties().forEach(property -> {
                    if (property.getValueId().equals(propertyValueId)) {
                        property.setValueName(propertyValueName);
                        updateSkus.add(sku);
                    }
                }));
        if (CollUtil.isEmpty(updateSkus)) {
            return 0;
        }

        productSkuMapper.updateBatch(updateSkus);
        return updateSkus.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkuList(Long spuId, List<ProductSkuSaveReqVO> skus) {
        // 构建属性与 SKU 的映射关系;
        Map<String, Long> existsSkuMap = convertMap(productSkuMapper.selectListBySpuId(spuId),
                ProductSkuConvert.INSTANCE::buildPropertyKey, ProductSkuDO::getId);

        // 拆分三个集合，新插入的、需要更新的、需要删除的
        List<ProductSkuDO> insertSkus = new ArrayList<>();
        List<ProductSkuDO> updateSkus = new ArrayList<>();
        List<ProductSkuDO> allUpdateSkus = BeanUtils.toBean(skus, ProductSkuDO.class, sku -> sku.setSpuId(spuId));
        allUpdateSkus.forEach(sku -> {
            String propertiesKey = ProductSkuConvert.INSTANCE.buildPropertyKey(sku);
            // 1、找得到的，进行更新
            Long existsSkuId = existsSkuMap.remove(propertiesKey);
            if (existsSkuId != null) {
                sku.setId(existsSkuId);
                updateSkus.add(sku);
                return;
            }
            // 2、找不到，进行插入
            sku.setSpuId(spuId);
            insertSkus.add(sku);
        });

        // 执行最终的批量操作
        if (CollUtil.isNotEmpty(insertSkus)) {
            productSkuMapper.insertBatch(insertSkus);
        }
        if (CollUtil.isNotEmpty(updateSkus)) {
            updateSkus.forEach(sku -> productSkuMapper.updateById(sku));
        }
        if (CollUtil.isNotEmpty(existsSkuMap)) {
            productSkuMapper.deleteByIds(existsSkuMap.values());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO) {
        // 更新 SKU 库存
        updateStockReqDTO.getItems().forEach(item -> {
            if (item.getIncrCount() > 0) {
                productSkuMapper.updateStockIncr(item.getId(), item.getIncrCount());
            } else if (item.getIncrCount() < 0) {
                int updateStockIncr = productSkuMapper.updateStockDecr(item.getId(), item.getIncrCount());
                if (updateStockIncr == 0) {
                    throw exception(SKU_STOCK_NOT_ENOUGH);
                }
            }
        });

        // 更新 SPU 库存
        List<ProductSkuDO> skus = productSkuMapper.selectByIds(
                convertSet(updateStockReqDTO.getItems(), ProductSkuUpdateStockReqDTO.Item::getId));
        Map<Long, Integer> spuStockIncrCounts = ProductSkuConvert.INSTANCE.convertSpuStockMap(
                updateStockReqDTO.getItems(), skus);
        productSpuService.updateSpuStock(spuStockIncrCounts);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserveSkuStock(ProductSkuStockLockReqDTO reqDTO) {
        StockFulfillment fulfillment = resolveStockFulfillment(reqDTO);
        if (CollUtil.isNotEmpty(fulfillment.wmsItems())) {
            productWmsInventoryAdapter.reserve(reqDTO.getOrderNo(), fulfillment.wmsItems());
        }
        updateLocalSkuStock(fulfillment.localItems(), -1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseSkuStock(ProductSkuStockLockReqDTO reqDTO) {
        StockFulfillment fulfillment = resolveStockFulfillment(reqDTO);
        if (CollUtil.isNotEmpty(fulfillment.wmsItems())) {
            productWmsInventoryAdapter.release(reqDTO.getOrderNo(), fulfillment.wmsItems());
        }
        updateLocalSkuStock(fulfillment.localItems(), 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outboundSkuStock(ProductSkuStockLockReqDTO reqDTO) {
        StockFulfillment fulfillment = resolveStockFulfillment(reqDTO);
        if (CollUtil.isNotEmpty(fulfillment.wmsItems())) {
            productWmsInventoryAdapter.outbound(reqDTO.getOrderNo(), fulfillment.wmsItems());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inboundReturnSkuStock(String returnNo, ProductSkuStockLockReqDTO reqDTO) {
        StockFulfillment fulfillment = resolveStockFulfillment(reqDTO);
        if (CollUtil.isNotEmpty(fulfillment.wmsItems())) {
            productWmsInventoryAdapter.inboundReturn(returnNo, reqDTO.getOrderNo(), fulfillment.wmsItems());
        }
    }

    @Override
    public List<ProductWmsStockReconciliationDTO> getWmsStockReconciliation(Collection<Long> spuIds) {
        List<ProductWmsStockReconciliationDTO> results = productSkuMapper.selectListWithWmsMapping(spuIds).stream()
                .map(this::reconcileWmsStock)
                .toList();
        if (results.isEmpty()) {
            return results;
        }
        Map<Long, String> spuNameMap = productSpuService.getSpuList(
                        results.stream().map(ProductWmsStockReconciliationDTO::getSpuId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(spu -> spu.getId(), spu -> spu.getName()));
        results.forEach(item -> item.setSpuName(spuNameMap.get(item.getSpuId())));
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncWmsStockCache(Collection<Long> spuIds) {
        Set<Long> changedSpuIds = new HashSet<>();
        int changedSkuCount = 0;
        for (ProductSkuDO sku : productSkuMapper.selectListWithWmsMapping(spuIds)) {
            ProductWmsInventoryAdapter.InventorySnapshot snapshot = productWmsInventoryAdapter.getSnapshot(
                    sku.getWmsSkuId(), sku.getWmsWarehouseId());
            // WMS 尚未建库存时不把商品库存误清零，由对账报告提示人工补齐映射或 WMS 库存。
            if (!snapshot.exists() || Objects.equals(sku.getStock(), snapshot.availableStock())) {
                continue;
            }
            productSkuMapper.updateStockCache(sku.getId(), snapshot.availableStock());
            changedSpuIds.add(sku.getSpuId());
            changedSkuCount++;
        }
        if (CollUtil.isNotEmpty(changedSpuIds)) {
            Map<Long, Integer> spuStockCounts = new HashMap<>();
            for (Long spuId : changedSpuIds) {
                int stock = productSkuMapper.selectListBySpuId(spuId).stream()
                        .map(ProductSkuDO::getStock)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .sum();
                spuStockCounts.put(spuId, stock);
            }
            productSpuService.syncSpuStock(spuStockCounts);
        }
        return changedSkuCount;
    }

    private ProductWmsStockReconciliationDTO reconcileWmsStock(ProductSkuDO sku) {
        ProductWmsInventoryAdapter.InventorySnapshot snapshot = productWmsInventoryAdapter.getSnapshot(
                sku.getWmsSkuId(), sku.getWmsWarehouseId());
        String status;
        if (!snapshot.exists()) {
            status = ProductWmsStockReconciliationDTO.STATUS_MISSING_INVENTORY;
        } else if (!Objects.equals(sku.getStock(), snapshot.availableStock())) {
            status = ProductWmsStockReconciliationDTO.STATUS_CACHE_DIFFERENCE;
        } else {
            status = ProductWmsStockReconciliationDTO.STATUS_NORMAL;
        }
        return new ProductWmsStockReconciliationDTO(sku.getId(), sku.getSpuId(), null, sku.getWmsSkuId(),
                sku.getWmsWarehouseId(), sku.getStock(), snapshot.physicalQuantity(), snapshot.lockedQuantity(),
                snapshot.availableStock(), status);
    }

    private StockFulfillment resolveStockFulfillment(ProductSkuStockLockReqDTO reqDTO) {
        Map<Long, Integer> skuCountMap = new LinkedHashMap<>();
        reqDTO.getItems().forEach(item -> {
            if (item.getCount() == null || item.getCount() <= 0) {
                throw exception(SKU_STOCK_NOT_ENOUGH);
            }
            skuCountMap.merge(item.getId(), item.getCount(), Integer::sum);
        });
        Map<Long, ProductSkuDO> skuMap = convertMap(productSkuMapper.selectByIds(skuCountMap.keySet()), ProductSkuDO::getId);
        if (skuMap.size() != skuCountMap.size()) {
            throw exception(SKU_NOT_EXISTS);
        }

        List<ProductSkuStockLockReqDTO.Item> localItems = new ArrayList<>();
        Map<WmsInventoryKey, Integer> wmsCountMap = new LinkedHashMap<>();
        skuCountMap.forEach((skuId, count) -> {
            ProductSkuDO sku = skuMap.get(skuId);
            validateWmsMapping(sku);
            if (sku.getWmsSkuId() == null) {
                ProductSkuStockLockReqDTO.Item localItem = new ProductSkuStockLockReqDTO.Item();
                localItem.setId(skuId).setCount(count);
                localItems.add(localItem);
                return;
            }
            wmsCountMap.merge(new WmsInventoryKey(sku.getWmsSkuId(), sku.getWmsWarehouseId()), count, Integer::sum);
        });
        List<ProductWmsInventoryAdapter.Item> wmsItems = wmsCountMap.entrySet().stream()
                .map(entry -> new ProductWmsInventoryAdapter.Item(entry.getKey().wmsSkuId(),
                        entry.getKey().warehouseId(), entry.getValue()))
                .toList();
        return new StockFulfillment(localItems, wmsItems);
    }

    private void updateLocalSkuStock(List<ProductSkuStockLockReqDTO.Item> localItems, int direction) {
        if (CollUtil.isEmpty(localItems)) {
            return;
        }
        List<ProductSkuUpdateStockReqDTO.Item> items = localItems.stream().map(item -> {
            ProductSkuUpdateStockReqDTO.Item stockItem = new ProductSkuUpdateStockReqDTO.Item();
            return stockItem.setId(item.getId()).setIncrCount(item.getCount() * direction);
        }).toList();
        updateSkuStock(new ProductSkuUpdateStockReqDTO(items));
    }

    private ProductSkuDO refreshWmsStock(ProductSkuDO sku) {
        if (sku == null || sku.getWmsSkuId() == null || sku.getWmsWarehouseId() == null) {
            return sku;
        }
        sku.setStock(productWmsInventoryAdapter.getAvailableStock(sku.getWmsSkuId(), sku.getWmsWarehouseId()));
        return sku;
    }

    private List<ProductSkuDO> refreshWmsStock(List<ProductSkuDO> skus) {
        skus.forEach(this::refreshWmsStock);
        return skus;
    }

    private void validateWmsMapping(ProductSkuSaveReqVO sku) {
        if ((sku.getWmsSkuId() == null) != (sku.getWmsWarehouseId() == null)) {
            throw exception(SKU_WMS_MAPPING_INVALID);
        }
    }

    private void validateWmsMapping(ProductSkuDO sku) {
        if ((sku.getWmsSkuId() == null) != (sku.getWmsWarehouseId() == null)) {
            throw exception(SKU_WMS_MAPPING_INVALID);
        }
    }

    private record StockFulfillment(List<ProductSkuStockLockReqDTO.Item> localItems,
                                    List<ProductWmsInventoryAdapter.Item> wmsItems) {
    }

    private record WmsInventoryKey(Long wmsSkuId, Long warehouseId) {
    }

}
