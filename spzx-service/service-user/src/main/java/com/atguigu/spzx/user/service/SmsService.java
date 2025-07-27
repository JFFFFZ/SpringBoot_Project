package com.atguigu.spzx.user.service;

public interface SmsService {
    // 发送验证码
    void sendValidateCode(String phoneNum);
}
