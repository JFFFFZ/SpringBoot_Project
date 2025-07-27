package com.atguigu.spzx.pay.mapper;

import com.atguigu.spzx.model.entity.pay.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentInfoMapper {
    // 根据订单号查询支付信息PaymentInfo
    PaymentInfo getByOrderNo(String orderNo);
    // 添加支付信息PaymentInfo
    void save(PaymentInfo paymentInfo);

    // 修改支付信息PaymentInfo
    void updatePaymentInfo(PaymentInfo paymentInfo);
}
