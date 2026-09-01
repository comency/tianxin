package cn.iocoder.yudao.module.trade.service.order.handler;

import cn.iocoder.yudao.module.product.api.sku.ProductSkuApi;
import cn.iocoder.yudao.module.product.api.sku.dto.ProductSkuStockLockReqDTO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import cn.iocoder.yudao.module.trade.service.order.TradeOrderQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeProductSkuOrderHandlerTest {

    @InjectMocks
    private TradeProductSkuOrderHandler handler;
    @Mock
    private ProductSkuApi productSkuApi;
    @Mock
    private TradeOrderQueryService tradeOrderQueryService;

    @Test
    void afterReceiveOrder_pickUp_shouldOutboundReservedStock() {
        TradeOrderDO order = createOrder(DeliveryTypeEnum.PICK_UP.getType());
        List<TradeOrderItemDO> items = List.of(createOrderItem());
        when(tradeOrderQueryService.getOrderItemListByOrderId(order.getId())).thenReturn(items);

        handler.afterReceiveOrder(order);

        ArgumentCaptor<ProductSkuStockLockReqDTO> captor = ArgumentCaptor.forClass(ProductSkuStockLockReqDTO.class);
        verify(productSkuApi).outboundSkuStock(captor.capture());
        assertEquals(order.getNo(), captor.getValue().getOrderNo());
        assertEquals(1, captor.getValue().getItems().size());
        assertEquals(items.get(0).getSkuId(), captor.getValue().getItems().get(0).getId());
        assertEquals(items.get(0).getCount(), captor.getValue().getItems().get(0).getCount());
    }

    @Test
    void afterReceiveOrder_express_shouldNotOutboundTwice() {
        TradeOrderDO order = createOrder(DeliveryTypeEnum.EXPRESS.getType());

        handler.afterReceiveOrder(order);

        verifyNoInteractions(productSkuApi, tradeOrderQueryService);
    }

    @Test
    void afterDeliveryOrder_express_shouldOutboundReservedStock() {
        TradeOrderDO order = createOrder(DeliveryTypeEnum.EXPRESS.getType());
        when(tradeOrderQueryService.getOrderItemListByOrderId(order.getId())).thenReturn(List.of(createOrderItem()));

        handler.afterDeliveryOrder(order);

        verify(productSkuApi).outboundSkuStock(any(ProductSkuStockLockReqDTO.class));
    }

    private static TradeOrderDO createOrder(Integer deliveryType) {
        return new TradeOrderDO().setId(100L).setNo("ORDER-100").setDeliveryType(deliveryType);
    }

    private static TradeOrderItemDO createOrderItem() {
        return new TradeOrderItemDO().setSkuId(200L).setCount(3);
    }

}
