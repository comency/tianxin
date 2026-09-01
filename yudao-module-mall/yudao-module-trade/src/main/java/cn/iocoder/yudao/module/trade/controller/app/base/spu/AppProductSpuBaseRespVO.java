package cn.iocoder.yudao.module.trade.controller.app.base.spu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品 SPU 基础 Response VO
 *
 * @author 芋道源码
 */
@Data
public class AppProductSpuBaseRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "所属店铺编号；0 表示平台自营", example = "1")
    private Long shopId;

    @Schema(description = "所属店铺名称快照", example = "恒泰新材料旗舰店")
    private String shopName;

    @Schema(description = "商品 SPU 名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    @Schema(description = "商品主图地址", example = "https://www.iocoder.cn/xx.png")
    private String picUrl;

    @Schema(description = "商品分类编号", example = "1")
    private Long categoryId;

    @Schema(description = "商品库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "10000")
    private Integer stock;

    @Schema(description = "商品状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

}
