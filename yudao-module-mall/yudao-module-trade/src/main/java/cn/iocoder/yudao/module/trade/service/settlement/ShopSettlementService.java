package cn.iocoder.yudao.module.trade.service.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementAuditReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementGenerateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementOrderDO;

import java.util.List;

public interface ShopSettlementService {

    Long generateSettlement(ShopSettlementGenerateReqVO reqVO);

    PageResult<ShopSettlementDO> getSettlementPage(ShopSettlementPageReqVO reqVO);

    ShopSettlementDO getSettlement(Long id);

    List<ShopSettlementOrderDO> getSettlementOrderList(Long settlementId);

    void auditSettlement(ShopSettlementAuditReqVO reqVO, Long auditUserId);

    void confirmSettlement(Long id, Long settleUserId);
}
