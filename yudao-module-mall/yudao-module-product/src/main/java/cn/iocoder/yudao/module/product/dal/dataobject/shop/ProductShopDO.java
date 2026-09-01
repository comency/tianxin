package cn.iocoder.yudao.module.product.dal.dataobject.shop;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 产业链企业店铺。
 *
 * @author DAMU
 */
@TableName("product_shop")
@KeySequence("product_shop_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductShopDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 企业名录编号，暂不建立跨模块物理外键 */
    private Long enterpriseId;
    private String name;
    private String logoUrl;
    private String contactName;
    private String contactMobile;
    /** 店铺负责人（后台系统用户编号）。一个店铺当前绑定一名主负责人。 */
    private Long managerUserId;
    private String introduction;
    /** {@link CommonStatusEnum} */
    private Integer status;
}
