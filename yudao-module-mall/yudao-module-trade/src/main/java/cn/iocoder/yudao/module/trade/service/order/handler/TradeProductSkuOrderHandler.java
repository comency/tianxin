package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.trade.convert.order.TradeOrderConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * 商品 SKU 库存的 {@link TradeOrderHandler} 实现类
 *
 * @author 芋道源码
 */
@Component
public class TradeProductSkuOrderHandler implements TradeOrderHandler {

    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        productSkuApi.reserveSkuStock(TradeOrderConvert.INSTANCE.convertStockLock(order.getNo(), orderItems));
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        productSkuApi.releaseSkuStock(TradeOrderConvert.INSTANCE.convertStockLock(order.getNo(), orderItems));
    }

    @Override
    public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO orderItem) {
        productSkuApi.releaseSkuStock(TradeOrderConvert.INSTANCE.convertStockLock(order.getNo(), singletonList(orderItem)));
    }

    @Override
    public void afterDeliveryOrder(TradeOrderDO order) {
        outboundOrderStock(order);
    }

    @Override
    public void afterReceiveOrder(TradeOrderDO order) {
        // 自提订单在核销时直接完成，不经过发货回调，因此需要在收货回调中将预占库存转为正式出库。
        // 快递订单已经在 afterDeliveryOrder 出库，不能在确认收货时重复扣减。
        if (!DeliveryTypeEnum.PICK_UP.getType().equals(order.getDeliveryType())) {
            return;
        }
        outboundOrderStock(order);
    }

    private void outboundOrderStock(TradeOrderDO order) {
        List<TradeOrderItemDO> orderItems = tradeOrderQueryService.getOrderItemListByOrderId(order.getId());
        productSkuApi.outboundSkuStock(TradeOrderConvert.INSTANCE.convertStockLock(order.getNo(), orderItems));
    }

}
