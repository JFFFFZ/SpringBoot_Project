package com.atguigu.spzx.manager.task;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.atguigu.spzx.manager.mapper.OrderInfoMapper;
import com.atguigu.spzx.manager.mapper.OrderStatisticsMapper;
import com.atguigu.spzx.model.entity.order.OrderStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component // 交给spring管理
public class OrderStatisticsTask {

    @Autowired
    private OrderInfoMapper orderInfoMapper ;

    @Autowired
    private OrderStatisticsMapper orderStatisticsMapper ;

    // 测试定时任务
    // 每隔5s 方法执行一次
    // 使用注解@Scheduled + cron 表达式（设置执行规则）
    // 推荐使用cron在线生成表达式  直接百度搜即可
//    @Scheduled(cron = "0/5 * * * * ?")   // 每隔5s执行一次
//    public void testHello() {
//        System.out.println(new Date().toInstant());
//    }

    // 每天凌晨两点 查询前一天的统计数据  并把统计数据存入到统计结果表当中
    @Scheduled(cron = "0 0 2 * * ? ")
    // @Scheduled(cron = "0/10 * * * * ?") // TODO c测试
    public void orderTotalAmountStatistics(){
        System.out.println(new Date().toInstant());
        // 1 获取前一天的日期
        String createDate = DateUtil.offsetDay(new Date(), -1).toString("yyyy-MM-dd");
        // 2 统计前一天的数据 （分组求和操作）
        OrderStatistics orderstatistics = orderInfoMapper.selectStatisticsByDate(createDate);
        // 3 将统计的数据存入统计结果表中
        if(orderstatistics!=null){
            orderStatisticsMapper.insert(orderstatistics);
        }
    }
}
