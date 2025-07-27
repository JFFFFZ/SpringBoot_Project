package com.atguigu.spzx.common.log.aspect;

import com.atguigu.spzx.common.log.annotation.Log;
import com.atguigu.spzx.common.log.service.AsyncOperLogService;
import com.atguigu.spzx.common.log.utils.LogUtil;
import com.atguigu.spzx.model.entity.system.SysOperLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogAspect {

    @Autowired
    private AsyncOperLogService asyncOperLogService;

    // 环绕通知
    @Around(value = "@annotation(sysLog)")
    public Object doAroundAdvice(ProceedingJoinPoint joinPoint, Log sysLog){

//        String title = sysLog.title();
//        int i = sysLog.businessType();
//        System.out.println("在业务方法之前输出：");
//        System.out.println("title:"+title+",businessType:"+i);

        // 业务方法调用之前 封装工具类
        SysOperLog sysOperLog = new SysOperLog();
        LogUtil.beforeHandleLog(sysLog,joinPoint,sysOperLog);
        // 业务逻辑执行
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();   // 手动触发业务逻辑程序的执行
            //System.out.println("在业务方法之后执行。");
            // 调用业务方法之后 封装数据
            LogUtil.afterHandlLog(sysLog,proceed,sysOperLog,0,null);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.afterHandlLog(sysLog,proceed,sysOperLog,1,e.getMessage());
            // 抛出运行时异常  否则事务不会感知
            throw new RuntimeException();
        } finally {
            // 调用Service中的方法 将日志数据添加到数据库中去
            asyncOperLogService.saveSysOperLog(sysOperLog);
        }

        return proceed;
    }
}
