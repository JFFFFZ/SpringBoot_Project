package com.atguigu.spzx.order;

import com.atguigu.spzx.common.anno.EnableUserLoginAuthInterceptor;
import com.atguigu.spzx.common.anno.EnableUserTokenFeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.atguigu.spzx"})
// 启动拦截器 在程序运行前 对feign包下的RequestTemplate对象的请求头中添加token
// 拦截器中会向RequestTemplate对象中添加token
@EnableUserTokenFeignInterceptor
// 启动拦截器 在程序运行前向线程池中放入userInfo对象  用于本微服务中使用
@EnableUserLoginAuthInterceptor
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }
}
