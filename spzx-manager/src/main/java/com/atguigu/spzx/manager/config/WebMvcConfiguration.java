package com.atguigu.spzx.manager.config;

import com.atguigu.spzx.manager.interceptor.LoginAuthInterceptor;

import com.atguigu.spzx.manager.properties.UserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 解决跨域的方式
@Component
public class WebMvcConfiguration implements WebMvcConfigurer {
    // 登录拦截器的相关配置  需要的话直接注释打开即可
    @Autowired
    private LoginAuthInterceptor loginAuthInterceptor;
    @Autowired
    private UserProperties userProperties;
    // 拦截器注册  配置哪些路径需要拦截 哪些路径不需要拦截
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginAuthInterceptor)
                .excludePathPatterns(userProperties.getNoAuthUrls()) // 下面的优化
//                .excludePathPatterns("/admin/system/index/login" ,
//                        "/admin/system/index/generateValidateCode") // 不需要拦截的路径
                .addPathPatterns("/**"); // 需要拦截的路径
    }

    // 实现跨域的相关配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")      // 添加路径规则
                .allowCredentials(true)               // 是否允许在跨域的情况下传递Cookie
                .allowedOriginPatterns("*")           // 允许请求来源的域规则
                .allowedMethods("*")
                .allowedHeaders("*");                // 允许所有的请求头
    }
}
