package com.atguigu.spzx.manager.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.utils.AuthContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

@Component
public class LoginAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    // 拦截器方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1 获取请求方式
        //   如果请求方式是options 即预检请求（看一看是否支持跨域） 直接放行
        // 2 从请求头中获取token
        // 3 如果token为空 返回错误提示
        // 4 token不为空 根据token去redis中获取用户信息
        // 5 如果redis查询不到用户信息，则返回错误提示
        // 6 如果redis中可以查询到用户信息，把用户信息放到threadlocal中
        // 7 更新redis中用户信息的过期时间
        // 8 放行 return true

        // 1 获取请求方式
        String method = request.getMethod();
        if("OPTIONS".equals(method)){
            //如果请求方式是options 即预检请求（看一看是否支持跨域） 直接放行
            return true;
        }
        //2 从请求头中获取token
        String token = request.getHeader("token");
        //3 如果token为空 返回错误提示
        if(StrUtil.isEmpty(token)){
            responseNoLoginInfo(response);
            return false;
        }
        // 4 token不为空 根据token去redis中获取用户信息
        String userInfoString = redisTemplate.opsForValue().get("user:login" + token);
        // 5 如果redis查询不到用户信息，则返回错误提示
        if(StrUtil.isEmpty(userInfoString)){
            responseNoLoginInfo(response);
            return false;
        }
        // 6 如果redis中可以查询到用户信息，把用户信息放到threadlocal中
        SysUser sysUser = JSON.parseObject(userInfoString, SysUser.class);
        AuthContextUtil.set(sysUser);
        // 7 更新redis中用户信息的过期时间
        redisTemplate.expire(
                "user:login" + token,
                30,
                TimeUnit.MINUTES);
        // 8 放行 return true
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        // 移除掉ThreadLocal中的数据（删除） ，避免出现内存溢出情况。
        AuthContextUtil.remove();
    }

    //响应208状态码给前端
    private void responseNoLoginInfo(HttpServletResponse response) {
        Result<Object> result = Result.build(null, ResultCodeEnum.LOGIN_AUTH);
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(JSON.toJSONString(result));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) writer.close();
        }
    }
}
