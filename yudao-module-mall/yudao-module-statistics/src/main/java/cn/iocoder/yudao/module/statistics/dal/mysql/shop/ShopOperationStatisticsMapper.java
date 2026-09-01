package cn.iocoder.yudao.module.statistics.dal.mysql.shop;

import cn.iocoder.yudao.module.statistics.controller.admin.shop.vo.ShopOperationStatisticsRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ShopOperationStatisticsMapper {

    List<ShopOperationStatisticsRespVO> selectOperationStatistics(@Param("shopId") Long shopId,
                                                                   @Param("beginTime") LocalDateTime beginTime,
                                                                   @Param("endTime") LocalDateTime endTime);
}
