package com.atguigu.spzx.gateway.filter;

import com.alibaba.fastjson2.JSONObject;
import com.atguigu.spzx.model.entity.user.UserInfo;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired
    private RedisTemplate<String , String> redisTemplate;

    // 路径匹配工具
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1 获取当前请求的路径
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        String path = uri.getPath();
        // 判断路径是否满足 "/api/**/auth/**" 即路径中是否包含了auth
        // 满足则进行登录校验 不满足则直接放行
        if(antPathMatcher.match("/api/**/auth/**",path)){
            // 登录校验
            UserInfo userInfo = this.getUserInfo(request);
            if(userInfo == null){
                //  未登录
                ServerHttpResponse response = exchange.getResponse();
                return out(response,ResultCodeEnum.LOGIN_AUTH);
            }
        }
        // 放行
        return chain.filter(exchange);
    }

    private UserInfo getUserInfo(ServerHttpRequest request) {
        String token = "";
        List<String> tokenList = request.getHeaders().get("token");
        if(tokenList!=null && tokenList.size()>0){
            token = tokenList.get(0);
        }
        // 判断token是否为空
        if(!StringUtils.hasText(token)){
            // 为空 则返回null
            return null;
        }
        // token不为空 则从redis中获取用户信息
        String userInfoJson = redisTemplate.opsForValue().get("user:spzx" + token);
        if(!StringUtils.hasText(userInfoJson)){
            // userJson 为空 则返回null
            return null;
        }
        // userInfoJson 不为空 则将JSON字符串转换成UserInfo对象
        UserInfo userInfo = JSONObject.parseObject(userInfoJson, UserInfo.class);
        return userInfo;
    }

    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        Result result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        //指定编码，否则在浏览器中会中文乱码
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
