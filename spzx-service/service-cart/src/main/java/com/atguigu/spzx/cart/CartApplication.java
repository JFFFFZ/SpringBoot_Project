package com.atguigu.spzx.cart;

import com.atguigu.spzx.common.anno.EnableUserLoginAuthInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

// 排除数据库的自动化配置
// (因为一旦在pom文件中导入MySQL，MyBatis依赖 运行时就会自动寻找配置，没有配置则报错因此要排除)，
// Cart微服务不需要访问数据库
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients(basePackages = {"com.atguigu.spzx"})  // 开启远程调用
@EnableUserLoginAuthInterceptor // 自定义注解  把userInfo 放到ThreadLocal中
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class,args);
    }
}
