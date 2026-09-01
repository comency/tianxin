package cn.iocoder.yudao.module.statistics.service.shop;

import cn.iocoder.yudao.module.statistics.controller.admin.shop.vo.ShopOperationStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.dal.mysql.shop.ShopOperationStatisticsMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopOperationStatisticsServiceImpl implements ShopOperationStatisticsService {

    @Resource
    private ShopOperationStatisticsMapper statisticsMapper;

    @Override
    public List<ShopOperationStatisticsRespVO> getOperationStatistics(Long shopId, LocalDateTime beginTime,
                                                                       LocalDateTime endTime) {
        LocalDateTime effectiveEnd = endTime == null ? LocalDateTime.now() : endTime;
        LocalDateTime effectiveBegin = beginTime == null ? effectiveEnd.minusDays(30) : beginTime;
        if (!effectiveBegin.isBefore(effectiveEnd)) {
            throw new IllegalArgumentException("统计开始时间必须早于结束时间");
        }
        return statisticsMapper.selectOperationStatistics(shopId, effectiveBegin, effectiveEnd);
    }
}
