package com.atguigu.spzx.common.exception;

import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

// 用于全局处理控制器的异常、数据绑定和数据校验。
// @ControllerAdvice 是 Spring 框架中的一个注解，用于定义全局的异常处理、数据绑定预处理或模型增强逻辑。
// 它通常与 @ExceptionHandler、@InitBinder 和 @ModelAttribute 结合使用，用于处理控制器层的通用逻辑。
// @ControllerAdvice 本质上是一个特殊的组件（由 @Component 注解标记），Spring 会自动扫描并注册它。
// 它会对所有的 @Controller 或 @RestController 生效。
// 可以通过 basePackages、assignableTypes 等属性限制其作用范围。
@ControllerAdvice
public class GlobalExceptionHandler {

    // 全局异常处理
    // 使用 @ExceptionHandler 注解来捕获和处理控制器中抛出的异常。
    //通过这种方式，可以避免在每个控制器中重复编写异常处理代码。
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.build(null, ResultCodeEnum.SYSTEM_ERROR);
    }

    // 自定义异常处理
    @ExceptionHandler(GuiguException.class)
    @ResponseBody
    public Result error(GuiguException e){
        return Result.build(null, e.getResultCodeEnum());
    }
}
