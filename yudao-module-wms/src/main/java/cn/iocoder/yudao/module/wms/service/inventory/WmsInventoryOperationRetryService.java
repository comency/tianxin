package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryOperationRetryDO;

import java.util.List;

/** WMS 库存操作失败记录及补偿服务。 */
public interface WmsInventoryOperationRetryService {

    void recordFailure(String operationType, String orderNo, String returnNo, String payload, Throwable error);

    int retryPending(int limit);

    /** 将人工处理记录重新放回队列并立即尝试一次。 */
    boolean retryById(Long id);

    List<WmsInventoryOperationRetryDO> getManualRecords(int limit);
}
