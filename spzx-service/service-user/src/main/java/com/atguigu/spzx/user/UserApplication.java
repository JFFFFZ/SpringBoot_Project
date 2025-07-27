package com.atguigu.spzx.user;

import com.atguigu.spzx.common.anno.EnableUserLoginAuthInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = {"com.atguigu.spzx"})
// @EnableDiscoveryClient
@EnableUserLoginAuthInterceptor // 启用用户登录认证拦截器
public class UserApplication {
    // appcode: 12b4e95054f2487aab7a0feeda0c7e7e
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }
}
