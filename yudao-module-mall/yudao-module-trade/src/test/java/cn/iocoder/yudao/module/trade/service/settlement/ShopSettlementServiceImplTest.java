package cn.iocoder.yudao.module.trade.service.settlement;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.product.api.shop.ProductShopApi;
import cn.iocoder.yudao.module.product.api.shop.dto.ProductShopRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementAuditReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementGenerateReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementOrderDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.settlement.ShopSettlementMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.settlement.ShopSettlementOrderMapper;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderStatusEnum;
import cn.iocoder.yudao.module.trade.enums.settlement.ShopSettlementStatusEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.SHOP_SETTLEMENT_NO_ELIGIBLE_ORDER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Import(ShopSettlementServiceImpl.class)
class ShopSettlementServiceImplTest extends BaseDbUnitTest {

    private static final Long SHOP_ID = 10L;
    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2026, 8, 1, 0, 0);
    private static final LocalDateTime PERIOD_END = LocalDateTime.of(2026, 8, 31, 23, 59, 59);

    @Resource
    private ShopSettlementServiceImpl settlementService;
    @Resource
    private ShopSettlementMapper settlementMapper;
    @Resource
    private ShopSettlementOrderMapper settlementOrderMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @MockitoBean
    private ProductShopApi productShopApi;

    @BeforeEach
    void setUpShop() {
        when(productShopApi.getShop(SHOP_ID)).thenReturn(new ProductShopRespDTO()
                .setId(SHOP_ID).setName("测试企业店铺"));
    }

    @Test
    void testGenerateSettlement_calculatesRefundAndCommission() {
        insertOrder("ORDER-1", 10000, 2000, LocalDateTime.of(2026, 8, 10, 12, 0));
        insertOrder("ORDER-2", 2000, 0, LocalDateTime.of(2026, 8, 20, 12, 0));
        insertOrder("OUTSIDE", 9000, 0, LocalDateTime.of(2026, 9, 1, 0, 0));

        Long settlementId = settlementService.generateSettlement(generateReq());

        ShopSettlementDO settlement = settlementMapper.selectById(settlementId);
        assertNotNull(settlement.getNo());
        assertEquals(2, settlement.getOrderCount());
        assertEquals(12000, settlement.getOrderPayAmount());
        assertEquals(2000, settlement.getRefundAmount());
        assertEquals(10000, settlement.getSettlementBaseAmount());
        assertEquals(500, settlement.getPlatformCommissionAmount());
        assertEquals(9500, settlement.getSettlementAmount());
        assertEquals(ShopSettlementStatusEnum.WAIT_AUDIT.getStatus(), settlement.getStatus());
        List<ShopSettlementOrderDO> details = settlementOrderMapper.selectListBySettlementId(settlementId);
        assertEquals(2, details.size());
        assertTrue(details.stream().allMatch(ShopSettlementOrderDO::getActive));
    }

    @Test
    void testGenerateSettlement_preventsDuplicateOrder() {
        insertOrder("ORDER-1", 10000, 0, LocalDateTime.of(2026, 8, 10, 12, 0));
        settlementService.generateSettlement(generateReq());

        assertServiceException(() -> settlementService.generateSettlement(generateReq()),
                SHOP_SETTLEMENT_NO_ELIGIBLE_ORDER);
    }

    @Test
    void testAuditAndConfirmSettlement() {
        insertOrder("ORDER-1", 10000, 0, LocalDateTime.of(2026, 8, 10, 12, 0));
        Long settlementId = settlementService.generateSettlement(generateReq());

        settlementService.auditSettlement(new ShopSettlementAuditReqVO().setId(settlementId)
                .setApproved(true).setAuditRemark("金额核对无误"), 100L);
        ShopSettlementDO audited = settlementMapper.selectById(settlementId);
        assertEquals(ShopSettlementStatusEnum.AUDITED.getStatus(), audited.getStatus());
        assertEquals(100L, audited.getAuditUserId());
        assertNotNull(audited.getAuditTime());

        settlementService.confirmSettlement(settlementId, 101L);
        ShopSettlementDO settled = settlementMapper.selectById(settlementId);
        assertEquals(ShopSettlementStatusEnum.SETTLED.getStatus(), settled.getStatus());
        assertEquals(101L, settled.getSettleUserId());
        assertNotNull(settled.getSettleTime());
    }

    @Test
    void testRejectSettlement_releasesOrdersForRegeneration() {
        insertOrder("ORDER-1", 10000, 0, LocalDateTime.of(2026, 8, 10, 12, 0));
        Long rejectedId = settlementService.generateSettlement(generateReq());
        settlementService.auditSettlement(new ShopSettlementAuditReqVO().setId(rejectedId)
                .setApproved(false).setAuditRemark("账期需要调整"), 100L);

        ShopSettlementOrderDO rejectedDetail = settlementOrderMapper.selectListBySettlementId(rejectedId).get(0);
        assertNull(rejectedDetail.getActive());
        Long regeneratedId = settlementService.generateSettlement(generateReq());
        assertNotEquals(rejectedId, regeneratedId);
    }

    @Test
    void testCalculateCommission_roundHalfUp() {
        assertEquals(1, ShopSettlementServiceImpl.calculateCommission(1, 5000));
        assertEquals(0, ShopSettlementServiceImpl.calculateCommission(1, 4999));
    }

    private ShopSettlementGenerateReqVO generateReq() {
        return new ShopSettlementGenerateReqVO().setShopId(SHOP_ID)
                .setPeriodStartTime(PERIOD_START).setPeriodEndTime(PERIOD_END).setCommissionRate(500);
    }

    private void insertOrder(String no, int payPrice, int refundPrice, LocalDateTime finishTime) {
        tradeOrderMapper.insert(TradeOrderDO.builder()
                .shopId(SHOP_ID).shopName("测试企业店铺").no(no).type(0).terminal(20)
                .userId(1L).userIp("127.0.0.1").status(TradeOrderStatusEnum.COMPLETED.getStatus())
                .productCount(1).finishTime(finishTime).commentStatus(false)
                .payStatus(true).payTime(finishTime.minusDays(1)).totalPrice(payPrice)
                .discountPrice(0).deliveryPrice(0).adjustPrice(0).payPrice(payPrice)
                .deliveryType(1).receiverName("测试用户").receiverMobile("13800000000")
                .receiverAreaId(1).receiverDetailAddress("测试地址").refundPrice(refundPrice)
                .couponId(0L).couponPrice(0).pointPrice(0).vipPrice(0).build());
    }
}
