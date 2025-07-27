package com.atguigu.spzx.manager.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import com.atguigu.spzx.manager.service.ValidateCodeService;
import com.atguigu.spzx.model.vo.system.ValidateCodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public ValidateCodeVo generateValidateCode(){
        // 生成图片验证码
        //1 通过工具（例如hutool）生成图片的验证码
        //2 将验证码保存到redis中，设置redis的key（uuid） 和redis的value（验证码值）
        //   设置过期时间
        //3 返回验证码对象ValidateCodeVo

        // 1 图片验证码
        CircleCaptcha circleCaptcha = CaptchaUtil.createCircleCaptcha(150, 48, 4, 2);
        String codeValue = circleCaptcha.getCode(); // 验证码的值
        String imageBase64 = circleCaptcha.getImageBase64();// 经过base64编码方式的 验证码的图片

        //2 将验证码保存到redis中，设置redis的key（uuid） 和redis的value（验证码值）
        String key = UUID.randomUUID().toString().replaceAll("-","");
        redisTemplate.opsForValue().set(
                "user:validate"+key,
                codeValue,
                5,
                TimeUnit.MINUTES);
        //3 返回验证码对象ValidateCodeVo
        ValidateCodeVo validateCodeVo = new ValidateCodeVo();
        validateCodeVo.setCodeKey(key); // redis中的存储验证码的 key
        validateCodeVo.setCodeValue("data:image/png;base64," + imageBase64);
        return validateCodeVo;
    }
}
