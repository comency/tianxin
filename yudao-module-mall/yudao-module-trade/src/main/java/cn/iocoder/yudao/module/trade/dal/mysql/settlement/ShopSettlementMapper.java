package cn.iocoder.yudao.module.trade.dal.mysql.settlement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.settlement.vo.ShopSettlementPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.settlement.ShopSettlementDO;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

@Mapper
public interface ShopSettlementMapper extends BaseMapperX<ShopSettlementDO> {

    default PageResult<ShopSettlementDO> selectPage(ShopSettlementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ShopSettlementDO>()
                .likeIfPresent(ShopSettlementDO::getNo, reqVO.getNo())
                .eqIfPresent(ShopSettlementDO::getShopId, reqVO.getShopId())
                .eqIfPresent(ShopSettlementDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ShopSettlementDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ShopSettlementDO::getId));
    }

    default int updateByIdAndStatus(Long id, Integer status, ShopSettlementDO update) {
        return update(update, new LambdaUpdateWrapper<ShopSettlementDO>()
                .eq(ShopSettlementDO::getId, id)
                .eq(ShopSettlementDO::getStatus, status));
    }
}
