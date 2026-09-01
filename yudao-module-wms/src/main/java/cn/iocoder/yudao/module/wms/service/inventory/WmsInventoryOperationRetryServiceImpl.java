package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryOperationRetryDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryOperationRetryMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 默认的本地 WMS 补偿实现；外部 WMS 接入时可复用记录和调度机制。 */
@Service
@Slf4j
public class WmsInventoryOperationRetryServiceImpl implements WmsInventoryOperationRetryService {

    private static final int MAX_AUTO_RETRY = 3;

    @Resource
    private WmsInventoryOperationRetryMapper retryMapper;
    @Resource
    private WmsMallInventoryService mallInventoryService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String operationType, String orderNo, String returnNo, String payload, Throwable error) {
        WmsInventoryOperationRetryDO record = new WmsInventoryOperationRetryDO()
                .setOperationType(operationType).setOrderNo(orderNo).setReturnNo(returnNo)
                .setPayload(payload).setStatus(WmsInventoryOperationRetryDO.STATUS_PENDING)
                .setRetryCount(0).setNextRetryTime(LocalDateTime.now().plusMinutes(1))
                .setLastError(error == null ? "未知错误" : trim(error.getMessage()));
        retryMapper.insert(record);
        log.warn("[recordFailure][WMS库存操作进入重试队列 operationType={}, orderNo={}, returnNo={}]",
                operationType, orderNo, returnNo, error);
    }

    @Override
    @Transactional
    public int retryPending(int limit) {
        int success = 0;
        for (WmsInventoryOperationRetryDO record : retryMapper.selectPending(LocalDateTime.now(), limit)) {
            record.setStatus(WmsInventoryOperationRetryDO.STATUS_RETRYING);
            retryMapper.updateById(record);
            try {
                execute(record);
                record.setStatus(WmsInventoryOperationRetryDO.STATUS_SUCCESS).setLastError(null);
                success++;
            } catch (Exception ex) {
                int count = record.getRetryCount() + 1;
                record.setRetryCount(count).setLastError(trim(ex.getMessage()));
                if (count >= MAX_AUTO_RETRY) {
                    record.setStatus(WmsInventoryOperationRetryDO.STATUS_MANUAL);
                } else {
                    record.setStatus(WmsInventoryOperationRetryDO.STATUS_PENDING)
                            .setNextRetryTime(LocalDateTime.now().plusMinutes((long) count * 5));
                }
                log.error("[retryPending][WMS库存操作重试失败 id={}, retryCount={}]", record.getId(), count, ex);
            }
            retryMapper.updateById(record);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean retryById(Long id) {
        WmsInventoryOperationRetryDO record = retryMapper.selectById(id);
        if (record == null || record.getStatus() == WmsInventoryOperationRetryDO.STATUS_SUCCESS) {
            return false;
        }
        record.setStatus(WmsInventoryOperationRetryDO.STATUS_PENDING).setNextRetryTime(LocalDateTime.now());
        retryMapper.updateById(record);
        return retryPending(1) == 1;
    }

    @Override
    public List<WmsInventoryOperationRetryDO> getManualRecords(int limit) {
        return retryMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WmsInventoryOperationRetryDO>()
                .eq(WmsInventoryOperationRetryDO::getStatus, WmsInventoryOperationRetryDO.STATUS_MANUAL)
                .orderByDesc(WmsInventoryOperationRetryDO::getUpdateTime)
                .last("LIMIT " + Math.max(1, Math.min(limit, 100))));
    }

    private void execute(WmsInventoryOperationRetryDO record) {
        Payload payload = JsonUtils.parseObject(record.getPayload(), Payload.class);
        List<WmsMallInventoryService.Item> items = payload.items().stream()
                .map(item -> new WmsMallInventoryService.Item(item.skuId(), item.warehouseId(), item.count())).toList();
        WmsInventoryOperationRetryContext.run(() -> {
            switch (record.getOperationType()) {
                case "RESERVE" -> mallInventoryService.reserve(record.getOrderNo(), items);
                case "RELEASE" -> mallInventoryService.release(record.getOrderNo(), items);
                case "OUTBOUND" -> mallInventoryService.outbound(record.getOrderNo(), items);
                case "INBOUND_RETURN" -> mallInventoryService.inboundReturn(record.getReturnNo(), record.getOrderNo(), items);
                default -> throw new IllegalArgumentException("未知 WMS 操作: " + record.getOperationType());
            }
        });
    }

    private static String trim(String message) {
        if (message == null) return "未知错误";
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    public record Payload(List<Item> items) {
        public record Item(Long skuId, Long warehouseId, Integer count) {}
    }
}
