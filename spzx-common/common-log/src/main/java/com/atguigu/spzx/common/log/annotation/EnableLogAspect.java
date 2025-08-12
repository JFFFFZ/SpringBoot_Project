package com.atguigu.spzx.common.log.annotation;

import com.atguigu.spzx.common.log.aspect.LogAspect;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// git修改测试
// 第二次修改
// 第三次修改
// 第四次修改
// 第五次修改
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(value = LogAspect.class)            // 通过Import注解导入日志切面类到Spring容器中
public @interface EnableLogAspect {

    // 测试git 第六次修改
}
