package com.study.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.room.common.Result;
import com.study.room.entity.User;

public interface UserService extends IService<User> {
    Result<String> sign(Long userId);
    Result<Integer> signCount(Long userId);
}
