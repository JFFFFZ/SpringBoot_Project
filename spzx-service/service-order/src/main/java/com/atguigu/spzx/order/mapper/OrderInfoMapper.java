package com.atguigu.spzx.order.mapper;

import com.atguigu.spzx.model.entity.order.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderInfoMapper {
    void save(OrderInfo orderInfo);

    // 根据订单id获取订单信息
    OrderInfo getById(Long orderId);

    // 根据userId和订单状态查询
    List<OrderInfo> findUserPage(Long userId, Integer orderStatus);

    // 根据订单id获取订单信息
    OrderInfo getOrderInfoByOrderNo(String orderNo);

    void updateById(OrderInfo orderInfo);
}
