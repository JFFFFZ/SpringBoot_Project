package com.atguigu.spzx.order.service;

import com.atguigu.spzx.model.dto.h5.OrderInfoDto;
import com.atguigu.spzx.model.entity.order.OrderInfo;
import com.atguigu.spzx.model.vo.h5.TradeVo;
import com.github.pagehelper.PageInfo;

public interface OrderInfoService {
    TradeVo getTrade();

    //提交订单,生成订单
    Long submitOrder(OrderInfoDto orderInfoDto);

    // 支付前的 获取订单信息
    OrderInfo getOrderInfo(Long orderId);

    // 商品页面 立即购买
    TradeVo buy(Long skuId);
    //获取订单分页列表
    PageInfo<OrderInfo> findUserPage(Integer page, Integer limit, Integer orderStatus);

    // 根据订单id获取订单信息
    OrderInfo getOrderInfoByOrderNo(String orderNo);

    // 更新订单状态
    void updateOrderStatus(String orderNo);
}
