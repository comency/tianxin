package cn.iocoder.yudao.module.trade.dal.mysql.settlement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementOrderDO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ShopSettlementOrderMapper extends BaseMapperX<ShopSettlementOrderDO> {

    default List<ShopSettlementOrderDO> selectListBySettlementId(Long settlementId) {
        return selectList(ShopSettlementOrderDO::getSettlementId, settlementId);
    }

    default List<ShopSettlementOrderDO> selectListByOrderIds(Collection<Long> orderIds) {
        return selectList(new LambdaQueryWrapperX<ShopSettlementOrderDO>()
                .inIfPresent(ShopSettlementOrderDO::getOrderId, orderIds)
                .eq(ShopSettlementOrderDO::getActive, true));
    }

    default int deactivateBySettlementId(Long settlementId) {
        return update(null, new LambdaUpdateWrapper<ShopSettlementOrderDO>()
                .set(ShopSettlementOrderDO::getActive, null)
                .eq(ShopSettlementOrderDO::getSettlementId, settlementId)
                .eq(ShopSettlementOrderDO::getActive, true));
    }
}
