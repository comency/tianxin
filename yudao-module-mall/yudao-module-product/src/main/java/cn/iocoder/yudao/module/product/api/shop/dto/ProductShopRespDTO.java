package cn.iocoder.yudao.module.product.api.shop.dto;

import lombok.Data;

/**
 * 企业店铺信息 Response DTO。
 *
 * @author DAMU
 */
@Data
public class ProductShopRespDTO {

    /** 店铺编号 */
    private Long id;
    /** 店铺名称 */
    private String name;
    /** 店铺 Logo */
    private String logoUrl;
    /** 店铺状态 */
    private Integer status;
    /** 店铺负责人（后台系统用户编号） */
    private Long managerUserId;

}
