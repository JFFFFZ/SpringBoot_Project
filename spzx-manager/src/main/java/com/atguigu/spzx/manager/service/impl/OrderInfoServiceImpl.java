package com.atguigu.spzx.manager.service.impl;

import cn.hutool.core.date.DateUtil;
import com.atguigu.spzx.manager.mapper.OrderStatisticsMapper;
import com.atguigu.spzx.manager.service.OrderInfoService;
import com.atguigu.spzx.model.dto.order.OrderStatisticsDto;
import com.atguigu.spzx.model.entity.order.OrderStatistics;
import com.atguigu.spzx.model.vo.order.OrderStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Autowired
    private OrderStatisticsMapper orderStatisticsMapper;

    @Override
    public OrderStatisticsVo getOrderStatisticsData(OrderStatisticsDto orderStatisticsDto) {
        // 根据dto条件查询统计结果数据  返回一个list集合
        List<OrderStatistics> orderStatisticsList = orderStatisticsMapper.selectList(orderStatisticsDto);
        // 遍历list集合 得到所有的日期 把所有的日期封装到一个list集合中
        List<String> dateList = orderStatisticsList
                .stream()
                .map(orderStatistics -> DateUtil.format(orderStatistics.getOrderDate(), "yyyy-MM-dd")) // 获取日期
                .collect(Collectors.toList());// 封装成一个新的list集合
        // 遍历list集合 得到日期对应的总金额 把所有的金额封装到一个list集合中
        List<BigDecimal> decimalList = orderStatisticsList
                .stream()
                .map(OrderStatistics::getTotalAmount)
                .collect(Collectors.toList());
        // 把两个list集合封装到OrderStatisticsVo对象中
        OrderStatisticsVo orderStatisticsVo = new OrderStatisticsVo();
        orderStatisticsVo.setDateList(dateList);
        orderStatisticsVo.setAmountList(decimalList);
        return orderStatisticsVo;
    }
}
