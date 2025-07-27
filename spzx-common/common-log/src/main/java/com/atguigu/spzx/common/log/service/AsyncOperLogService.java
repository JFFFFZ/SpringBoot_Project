package com.atguigu.spzx.common.log.service;

import com.atguigu.spzx.model.entity.system.SysOperLog;

// 保存日志数据
public interface AsyncOperLogService {
    public abstract void saveSysOperLog(SysOperLog sysOperLog);
}
