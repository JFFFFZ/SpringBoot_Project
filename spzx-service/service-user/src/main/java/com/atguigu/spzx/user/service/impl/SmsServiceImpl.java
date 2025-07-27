package com.atguigu.spzx.user.service.impl;

import com.atguigu.spzx.user.service.SmsService;
import com.atguigu.spzx.utils.HttpUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements SmsService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 发送验证码
    @Override
    public void sendValidateCode(String phoneNum) {
        // 判断redis中是否存在验证码 若存在直接return
        String code = redisTemplate.opsForValue().get(phoneNum);
        if(!StringUtils.hasText(code)){
            return;
        }
        // 非空
        // 1 生成验证码  使用验证码工具类
        String validateCode = RandomStringUtils.randomNumeric(4);
        // 2 如果当前的手机号码已经存在验证码，则删除/修改之前的验证码 保存当前验证码到redis中 并设置过期时间
        redisTemplate.opsForValue().set(phoneNum, validateCode, 1, TimeUnit.MINUTES);

        // 3 远程调用阿里云中的验证码api 将验证码发送至手机
        sendMessage(phoneNum, validateCode);
    }

    private void sendMessage(String phoneNum, String validateCode) {
        //以精品服务打造精品API产品。
        //vip优惠券，技术支持。请直接联系客服（vx同号）  15622205140。
        String host = "https://dfsns.market.alicloudapi.com";
        String path = "/data/send_sms";
        String method = "POST";
        String appcode = "12b4e95054f2487aab7a0feeda0c7e7e";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", "code:" + validateCode);
        bodys.put("template_id", "CST_ptdie100");  //注意，CST_ptdie100该模板ID仅为调试使用，调试结果为"status": "OK" ，即表示接口调用成功，然后联系客服报备自己的专属签名模板ID，以保证短信稳定下发
        bodys.put("phone_number", phoneNum);
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            System.out.println(result);
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         *重要提示：
         *如您的返回结果中，没有我们接口的返回报文，或者连header的信息都打印出来了。可能是您的代码环境未能适配该请求示例。
         *那么，以下两个命令行，您可以二选一，选择一个适合你环境的加入到请求示例中。即可打印我们接口的返回报文。
         *或者直接联系客服  VX 15622205140
         *
         *System.out.println(EntityUtils.toString(response.getEntity()));
         *
         *System.out.println(response.body().string());
         */
    }
}
