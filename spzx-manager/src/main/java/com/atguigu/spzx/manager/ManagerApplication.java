package com.atguigu.spzx.manager;

import com.atguigu.spzx.common.log.annotation.EnableLogAspect;
import com.atguigu.spzx.manager.properties.MinioProperties;
import com.atguigu.spzx.manager.properties.UserProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

//@SpringBootApplication(scanBasePackages = {"com.atguigu.spzx"})

@EnableLogAspect   // 自定义注解  目的是导入@Import(value = LogAspect.class)
@EnableScheduling // 开启定时任务功能
@EnableConfigurationProperties(value = {UserProperties.class, MinioProperties.class})
@SpringBootApplication
@ComponentScan(basePackages = {"com.atguigu.spzx"})
public class ManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class,args);
    }
}
