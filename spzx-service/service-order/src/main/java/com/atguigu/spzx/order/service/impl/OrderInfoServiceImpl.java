package com.atguigu.spzx.order.service.impl;

import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.feign.cart.CartFeignClient;
import com.atguigu.spzx.feign.product.ProductFeignClient;
import com.atguigu.spzx.feign.user.UserFeignClient;
import com.atguigu.spzx.model.dto.h5.OrderInfoDto;
import com.atguigu.spzx.model.entity.h5.CartInfo;
import com.atguigu.spzx.model.entity.order.OrderInfo;
import com.atguigu.spzx.model.entity.order.OrderItem;
import com.atguigu.spzx.model.entity.order.OrderLog;
import com.atguigu.spzx.model.entity.product.ProductSku;
import com.atguigu.spzx.model.entity.user.UserAddress;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.h5.TradeVo;
import com.atguigu.spzx.order.mapper.OrderInfoMapper;
import com.atguigu.spzx.order.mapper.OrderItemMapper;
import com.atguigu.spzx.order.mapper.OrderLogMapper;
import com.atguigu.spzx.order.service.OrderInfoService;
import com.atguigu.spzx.utils.AuthContextUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

    @Autowired
    private CartFeignClient cartFeignClient;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    // 从购物车下单
    @Override
    public TradeVo getTrade() {
        // 1 向购物车微服务发起请求  远程调用  获取购物车中选中的商品列表
        List<CartInfo> cartInfoList = cartFeignClient.getAllCkecked();
        // 2 封装数据至vo对象 用于返回
        TradeVo tradeVo = new TradeVo();
        // 获取选中的商品
        List<OrderItem> orderItemList = new ArrayList<>();
        cartInfoList.forEach(cartInfo -> {
            OrderItem orderItem = new OrderItem();
            // 封装数据
            orderItem.setSkuId(cartInfo.getSkuId());
            orderItem.setSkuName(cartInfo.getSkuName());
            orderItem.setSkuNum(cartInfo.getSkuNum());
            orderItem.setSkuPrice(cartInfo.getCartPrice());
            orderItem.setThumbImg(cartInfo.getImgUrl());
            // 封装到集合中
            orderItemList.add(orderItem);
        });
        // 获取总金额 遍历orderItemList 将每个商品的 单价*数量 累加
        BigDecimal totalAmount = new BigDecimal(0);
        for(OrderItem orderItem : orderItemList){
            totalAmount = totalAmount.add(
                    orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum()))
            );
        }
        tradeVo.setOrderItemList(orderItemList);
        tradeVo.setTotalAmount(totalAmount);
        return tradeVo;
    }

    //提交订单,生成订单 要操作多个表  推荐使用事务
    @Override
    public Long submitOrder(OrderInfoDto orderInfoDto) {
        // 1 获取 要保存到数据库的order_item表中的信息 购物车选中的商品列表 dto中有
        List<OrderItem> orderItemList = orderInfoDto.getOrderItemList();
        // 若为空 则抛出异常 购买商品（orderItem）不能为空
        if(CollectionUtils.isEmpty(orderItemList)){
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
        // 2 校验商品库存 根据skuId查找数据库 将订单中的orderItem的skuNum 和数据库中的数量进行比对
        for (OrderItem orderItem : orderItemList) {
            Long skuId = orderItem.getSkuId();
            // 根据skuId查找数据库 使用远程调用实现  // 微服务隔离
            ProductSku productSku = productFeignClient.getBySkuId(skuId);
            Integer stockNum = productSku.getStockNum();// 库存数量
            Integer orderSkuNum = orderItem.getSkuNum();// 订单数量
            if(stockNum.intValue() < orderSkuNum.intValue()){
                // 超出库存 抛出异常
                throw new GuiguException(ResultCodeEnum.STOCK_LESS);
            }
        }
        // 3 使用mapper保存到本地数据库  注意 回显订单id
        // 保存到order_info表中的信息
        OrderInfo orderInfo = new OrderInfo();
        // 封装数据   从线程池中取数据
        UserInfo userInfo = AuthContextUtil.getUserInfo();
        Long userId = userInfo.getId();
        orderInfo.setUserId(userId);// 用户id
        orderInfo.setOrderNo(String.valueOf(System.currentTimeMillis()));//订单编号
        orderInfo.setNickName(userInfo.getNickName());//用户昵称
        Long userAddressId = orderInfoDto.getUserAddressId();// 从dto中获取地址id
        // TODO: 远程调用实现  用地址id 查找地址信息
        UserAddress userAddress = userFeignClient.getUserAddress(userAddressId);
        orderInfo.setReceiverName(userAddress.getName());
        orderInfo.setReceiverPhone(userAddress.getPhone());
        orderInfo.setReceiverTagName(userAddress.getTagName());
        orderInfo.setReceiverProvince(userAddress.getProvinceCode());
        orderInfo.setReceiverCity(userAddress.getCityCode());
        orderInfo.setReceiverDistrict(userAddress.getDistrictCode());
        orderInfo.setReceiverAddress(userAddress.getFullAddress());
        //订单金额
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderItem orderItem : orderItemList) {
            totalAmount = totalAmount.add(orderItem.getSkuPrice().multiply(new BigDecimal(orderItem.getSkuNum())));
        }
        orderInfo.setTotalAmount(totalAmount);
        orderInfo.setCouponAmount(new BigDecimal(0));
        orderInfo.setOriginalTotalAmount(totalAmount);
        orderInfo.setFeightFee(orderInfoDto.getFeightFee());
        orderInfo.setPayType(2);
        orderInfo.setOrderStatus(0);
        // 保存
        orderInfoMapper.save(orderInfo);

        // 保存到order_item表中的信息
        for (OrderItem orderItem : orderItemList) {
            // 设置对应的订单id
            orderItem.setOrderId(orderInfo.getId());
            orderItemMapper.save(orderItem);
        }
        // 保存到order_log表中的信息
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderInfo.getId());
        orderLog.setProcessStatus(0);
        orderLog.setNote("提交订单");
        orderLogMapper.save(orderLog);

        // TODO 4 把已经生成订单的商品从购物车中删除 用远程调用实现 要调用购物车的微服务
        cartFeignClient.deleteChecked();
        // 6 返回订单id
        return orderInfo.getId();
    }

    // 支付前的 获取订单信息
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return orderInfoMapper.getById(orderId);
    }

    // 商品页面 立即购买
    @Override
    public TradeVo buy(Long skuId) {
        // 查询商品
        ProductSku productSku = productFeignClient.getBySkuId(skuId);
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = new OrderItem();
        orderItem.setSkuId(skuId);
        orderItem.setSkuName(productSku.getSkuName());
        orderItem.setSkuNum(1);
        orderItem.setSkuPrice(productSku.getSalePrice());
        orderItem.setThumbImg(productSku.getThumbImg());
        orderItemList.add(orderItem);

        // 计算总金额
        BigDecimal totalAmount = productSku.getSalePrice();
        TradeVo tradeVo = new TradeVo();
        tradeVo.setTotalAmount(totalAmount);
        tradeVo.setOrderItemList(orderItemList);

        // 返回
        return tradeVo;
    }

    //  获取订单分页列表
    @Override
    public PageInfo<OrderInfo> findUserPage(Integer page, Integer limit, Integer orderStatus) {
        PageHelper.startPage(page,limit);
        // 获取全部页
        Long userId = AuthContextUtil.getUserInfo().getId();
        List<OrderInfo> orderInfoList = orderInfoMapper.findUserPage(userId, orderStatus);

        orderInfoList.forEach(orderInfo -> {
            List<OrderItem> orderItem = orderItemMapper.findByOrderId(orderInfo.getId());
            orderInfo.setOrderItemList(orderItem);
        });
        // 使用new PageInfo(list) 把list转换成PageInfo对象
        return new PageInfo<>(orderInfoList);
    }

    // 根据订单id获取订单信息
    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.getOrderInfoByOrderNo(orderNo);
        List<OrderItem> orderItemList = orderItemMapper.findByOrderId(orderInfo.getId());
        orderInfo.setOrderItemList(orderItemList);
        return orderInfo;
    }

    // 更新订单状态
    @Transactional
    @Override
    public void updateOrderStatus(String orderNo) {
        // 更新订单状态
        OrderInfo orderInfo = orderInfoMapper.getOrderInfoByOrderNo(orderNo);
        orderInfo.setOrderStatus(1);
        orderInfo.setPayType(2);
        orderInfo.setPaymentTime(new Date());
        orderInfoMapper.updateById(orderInfo);

        // 记录日志
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderInfo.getId());
        orderLog.setProcessStatus(1);
        orderLog.setNote("支付宝支付成功");
        orderLogMapper.save(orderLog);
    }
}
