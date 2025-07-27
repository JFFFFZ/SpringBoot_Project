package com.atguigu.spzx.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserTokenOpenFeignIntercepotr implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 弥补feign调用时，丢失token的问题
        // 来自 网关过来的请求
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        // 关键步骤  向请求头中添加token字段   使得feign调用时携带token
        // feign使用的请求  注意不是HttpServletRequest  而是RequestTemplate 来自feign包
        requestTemplate.header("token",token);
        // 添加完token后，feign调用时，会携带token，服务端会根据token获取用户信息，从而实现用户登录状态的共享

    }
}
