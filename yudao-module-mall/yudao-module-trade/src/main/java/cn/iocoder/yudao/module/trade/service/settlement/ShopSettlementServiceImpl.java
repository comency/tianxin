package cn.iocoder.yudao.module.trade.service.settlement;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.shop.ProductShopApi;
import cn.iocoder.yudao.module.product.api.shop.dto.ProductShopRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementAuditReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementGenerateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementOrderDO;
import cn.iocoder.yudao.module.trade.dal.mysql.order.TradeOrderMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.settlement.ShopSettlementMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.settlement.ShopSettlementOrderMapper;
import cn.iocoder.yudao.module.trade.enums.settlement.ShopSettlementStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

@Service
@Validated
public class ShopSettlementServiceImpl implements ShopSettlementService {

    private static final BigDecimal COMMISSION_RATE_BASE = BigDecimal.valueOf(10000);

    @Resource
    private ShopSettlementMapper settlementMapper;
    @Resource
    private ShopSettlementOrderMapper settlementOrderMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private ProductShopApi productShopApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateSettlement(ShopSettlementGenerateReqVO reqVO) {
        if (!reqVO.getPeriodStartTime().isBefore(reqVO.getPeriodEndTime())) {
            throw exception(SHOP_SETTLEMENT_PERIOD_INVALID);
        }
        ProductShopRespDTO shop = productShopApi.getShop(reqVO.getShopId());
        if (shop == null) {
            throw exception(SHOP_SETTLEMENT_SHOP_NOT_EXISTS);
        }

        List<TradeOrderDO> orders = tradeOrderMapper.selectListForShopSettlement(reqVO.getShopId(),
                reqVO.getPeriodStartTime(), reqVO.getPeriodEndTime());
        Set<Long> occupiedOrderIds = orders.isEmpty() ? Set.of() : convertSet(
                settlementOrderMapper.selectListByOrderIds(convertSet(orders, TradeOrderDO::getId)),
                ShopSettlementOrderDO::getOrderId);
        orders.removeIf(order -> occupiedOrderIds.contains(order.getId()));
        if (orders.isEmpty()) {
            throw exception(SHOP_SETTLEMENT_NO_ELIGIBLE_ORDER);
        }

        List<ShopSettlementOrderDO> details = orders.stream().map(order -> createOrderDetail(
                order, reqVO.getShopId(), reqVO.getCommissionRate())).toList();
        int orderPayAmount = sum(details, ShopSettlementOrderDO::getPayAmount);
        int refundAmount = sum(details, ShopSettlementOrderDO::getRefundAmount);
        int settlementBaseAmount = sum(details, ShopSettlementOrderDO::getSettlementBaseAmount);
        int platformCommissionAmount = sum(details, ShopSettlementOrderDO::getPlatformCommissionAmount);
        int settlementAmount = sum(details, ShopSettlementOrderDO::getSettlementAmount);

        ShopSettlementDO settlement = ShopSettlementDO.builder()
                .no("JS" + IdUtil.getSnowflakeNextIdStr())
                .shopId(shop.getId()).shopName(shop.getName())
                .periodStartTime(reqVO.getPeriodStartTime()).periodEndTime(reqVO.getPeriodEndTime())
                .orderCount(details.size()).orderPayAmount(orderPayAmount).refundAmount(refundAmount)
                .settlementBaseAmount(settlementBaseAmount).commissionRate(reqVO.getCommissionRate())
                .platformCommissionAmount(platformCommissionAmount).settlementAmount(settlementAmount)
                .status(ShopSettlementStatusEnum.WAIT_AUDIT.getStatus()).build();
        settlementMapper.insert(settlement);
        details.forEach(detail -> detail.setSettlementId(settlement.getId()));
        settlementOrderMapper.insertBatch(details);
        return settlement.getId();
    }

    private ShopSettlementOrderDO createOrderDetail(TradeOrderDO order, Long shopId, Integer commissionRate) {
        int payAmount = valueOrZero(order.getPayPrice());
        int refundAmount = Math.min(payAmount, valueOrZero(order.getRefundPrice()));
        int settlementBaseAmount = Math.max(0, payAmount - refundAmount);
        int platformCommissionAmount = calculateCommission(settlementBaseAmount, commissionRate);
        return ShopSettlementOrderDO.builder().shopId(shopId).orderId(order.getId()).orderNo(order.getNo())
                .orderFinishTime(order.getFinishTime()).payAmount(payAmount).refundAmount(refundAmount)
                .settlementBaseAmount(settlementBaseAmount).platformCommissionAmount(platformCommissionAmount)
                .settlementAmount(settlementBaseAmount - platformCommissionAmount).active(true).build();
    }

    static int calculateCommission(int settlementBaseAmount, int commissionRate) {
        return BigDecimal.valueOf(settlementBaseAmount).multiply(BigDecimal.valueOf(commissionRate))
                .divide(COMMISSION_RATE_BASE, 0, RoundingMode.HALF_UP).intValueExact();
    }

    private static int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static int sum(List<ShopSettlementOrderDO> details,
                           java.util.function.Function<ShopSettlementOrderDO, Integer> getter) {
        return details.stream().map(getter).mapToInt(Integer::intValue).sum();
    }

    @Override
    public PageResult<ShopSettlementDO> getSettlementPage(ShopSettlementPageReqVO reqVO) {
        return settlementMapper.selectPage(reqVO);
    }

    @Override
    public ShopSettlementDO getSettlement(Long id) {
        return settlementMapper.selectById(id);
    }

    @Override
    public List<ShopSettlementOrderDO> getSettlementOrderList(Long settlementId) {
        return settlementOrderMapper.selectListBySettlementId(settlementId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditSettlement(ShopSettlementAuditReqVO reqVO, Long auditUserId) {
        validateSettlementExists(reqVO.getId());
        Integer targetStatus = Boolean.TRUE.equals(reqVO.getApproved())
                ? ShopSettlementStatusEnum.AUDITED.getStatus() : ShopSettlementStatusEnum.REJECTED.getStatus();
        ShopSettlementDO update = new ShopSettlementDO().setStatus(targetStatus).setAuditUserId(auditUserId)
                .setAuditTime(LocalDateTime.now()).setAuditRemark(reqVO.getAuditRemark());
        if (settlementMapper.updateByIdAndStatus(reqVO.getId(),
                ShopSettlementStatusEnum.WAIT_AUDIT.getStatus(), update) == 0) {
            throw exception(SHOP_SETTLEMENT_STATUS_NOT_WAIT_AUDIT);
        }
        if (!Boolean.TRUE.equals(reqVO.getApproved())) {
            settlementOrderMapper.deactivateBySettlementId(reqVO.getId());
        }
    }

    @Override
    public void confirmSettlement(Long id, Long settleUserId) {
        validateSettlementExists(id);
        ShopSettlementDO update = new ShopSettlementDO().setStatus(ShopSettlementStatusEnum.SETTLED.getStatus())
                .setSettleUserId(settleUserId).setSettleTime(LocalDateTime.now());
        if (settlementMapper.updateByIdAndStatus(id, ShopSettlementStatusEnum.AUDITED.getStatus(), update) == 0) {
            throw exception(SHOP_SETTLEMENT_STATUS_NOT_AUDITED);
        }
    }

    private void validateSettlementExists(Long id) {
        if (settlementMapper.selectById(id) == null) {
            throw exception(SHOP_SETTLEMENT_NOT_EXISTS);
        }
    }
}
