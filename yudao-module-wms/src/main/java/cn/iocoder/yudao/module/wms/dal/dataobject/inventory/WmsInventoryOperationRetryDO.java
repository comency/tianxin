package cn.iocoder.yudao.module.wms.dal.dataobject.inventory;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** WMS 库存操作失败后的重试/人工补偿记录。 */
@TableName("wms_inventory_operation_retry")
@TenantIgnore
@KeySequence("wms_inventory_operation_retry_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class WmsInventoryOperationRetryDO extends BaseDO {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_RETRYING = 1;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_MANUAL = 3;

    @TableId
    private Long id;
    private String operationType;
    private String orderNo;
    private String returnNo;
    private String payload;
    private Integer status;
    private Integer retryCount;
    private LocalDateTime nextRetryTime;
    private String lastError;
}
