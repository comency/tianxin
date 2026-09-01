package cn.iocoder.yudao.module.wms.dal.mysql.inventory;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryOperationRetryDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WmsInventoryOperationRetryMapper extends BaseMapperX<WmsInventoryOperationRetryDO> {

    default List<WmsInventoryOperationRetryDO> selectPending(LocalDateTime now, int limit) {
        return selectList(new LambdaQueryWrapper<WmsInventoryOperationRetryDO>()
                .eq(WmsInventoryOperationRetryDO::getStatus, WmsInventoryOperationRetryDO.STATUS_PENDING)
                .le(WmsInventoryOperationRetryDO::getNextRetryTime, now)
                .orderByAsc(WmsInventoryOperationRetryDO::getNextRetryTime)
                .last("LIMIT " + Math.max(1, Math.min(limit, 100))));
    }
}
