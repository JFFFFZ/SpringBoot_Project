package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.manager.service.SysMenuService;
import com.atguigu.spzx.manager.service.SysUserService;
import com.atguigu.spzx.manager.service.ValidateCodeService;
import com.atguigu.spzx.model.dto.system.LoginDto;
import com.atguigu.spzx.model.entity.system.SysUser;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.system.LoginVo;
import com.atguigu.spzx.model.vo.system.SysMenuVo;
import com.atguigu.spzx.model.vo.system.ValidateCodeVo;
import com.atguigu.spzx.utils.AuthContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户接口")//  swagger中设置名称 不重要
@RestController // 交给spring管理 返回json数据
@RequestMapping("/admin/system/index")  // 请求路径为 /admin/system/index时执行
public class IndexController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ValidateCodeService validateCodeService;

    @Autowired
    private SysMenuService sysMenuService;

    // 用户登录 提交方式为post
    @Operation(summary = "用户登录的方法")  // 在swagger中显示的方法名的别名 不写则默认是方法名
    @PostMapping("login")
    public Result login(@RequestBody LoginDto loginDto) {
        LoginVo loginVo = sysUserService.login(loginDto);
        return Result.build(loginVo, ResultCodeEnum.SUCCESS);
    }

    // 用户退出登录
    @GetMapping(value = "/logout")
    public Result logout(@RequestHeader(name = "token") String token) {
        sysUserService.logout(token);
        return Result.build(null, ResultCodeEnum.SUCCESS);

    }

    // 生成图片验证码
    @GetMapping(value = "/generateValidateCode")
    public Result<ValidateCodeVo> generateValidateCode() {
        ValidateCodeVo validateCodeVo = validateCodeService.generateValidateCode();
        return Result.build(validateCodeVo, ResultCodeEnum.SUCCESS);
    }

//    // 从Redis中获取当前登录用户的信息
//    @GetMapping(value = "/getUserInfo")
//    public Result<SysUser> getUserInfo(@RequestHeader(name = "token") String token){
//    //public Result<SysUser> getUserInfo(HttpServletRequest request){
//        // 1 从请求头中获取token 当传入的参数为HttpServletRequest request时
//        // String token = request.getHeader("token");
//        // 2 根据token查用户信息
//        SysUser sysUser = sysUserService.getUserInfo(token);
//        // 3 返回用户信息
//        return Result.build(sysUser, ResultCodeEnum.SUCCESS);
//    }

    // 从ThreadLocal中获取当前登录用户的信息
    // 因为用户登录后会在ThreadLocal中保存用户信息
    // 因此如果能从ThreadLocal中获得用户信息则说明当前处于登录状态  如果没有则说明当前处于未登录状态
    @GetMapping(value = "/getUserInfo")
    public Result<SysUser> getUserInfo() {
        // 从ThreadLocal中获取当前登录用户的信息
        SysUser sysUser = AuthContextUtil.get();
        return Result.build(sysUser, ResultCodeEnum.SUCCESS);
    }

    // 查询用户可以操作的菜单列表
    @GetMapping("/menus")
    public Result menus() {
        List<SysMenuVo> list = sysMenuService.findMenusByUserId();
        return Result.build(list, ResultCodeEnum.SUCCESS);
    }


}
