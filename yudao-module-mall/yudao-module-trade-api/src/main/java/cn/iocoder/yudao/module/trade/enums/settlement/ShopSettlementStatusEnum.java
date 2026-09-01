package cn.iocoder.yudao.module.trade.enums.settlement;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/** 店铺结算单状态。 */
@Getter
@AllArgsConstructor
public enum ShopSettlementStatusEnum implements ArrayValuable<Integer> {

    WAIT_AUDIT(0, "待审核"),
    AUDITED(10, "已审核"),
    SETTLED(20, "已结算"),
    REJECTED(30, "已驳回");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ShopSettlementStatusEnum::getStatus)
            .toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
