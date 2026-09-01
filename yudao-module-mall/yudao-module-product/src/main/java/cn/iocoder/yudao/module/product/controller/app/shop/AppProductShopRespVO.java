package cn.iocoder.yudao.module.product.controller.app.shop;

import lombok.Data;

@Data
public class AppProductShopRespVO {
    private Long id;
    private String name;
    private String logoUrl;
    private String introduction;
    private String contactName;
    private String contactMobile;
}
