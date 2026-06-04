package com.study.room.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.room.common.Result;
import com.study.room.entity.User;
import com.study.room.mapper.UserMapper;
import com.study.room.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 1. 每日签到打卡 (Redis BitMap)
    @Override
    public Result<String> sign(Long userId) {
        // 获取当前年月做为Key的后缀，例如 202604
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "user:sign:" + userId + keySuffix;

        // 获取今天是本月的第几天（1~31）
        int dayOfMonth = now.getDayOfMonth();
        
        // BitMap 下标从0开始，所以 dayOfMonth - 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.success("签到成功！");
    }

    // 2. 统计连续签到天数
    @Override
    public Result<Integer> signCount(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = "user:sign:" + userId + keySuffix;
        int dayOfMonth = now.getDayOfMonth();

        // 获取从第1天到今天的签到记录（返回一个十进制数字）
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );

        if (result == null || result.isEmpty() || result.get(0) == null || result.get(0) == 0) {
            return Result.success(0);
        }

        Long num = result.get(0);
        int count = 0;
        // 循环遍历，与1做与运算，判断最后一位是否为1
        while (true) {
            if ((num & 1) == 0) {
                break; // 如果为0，说明连续签到中断
            } else {
                count++; // 如果为1，连续签到天数+1
            }
            num >>>= 1; // 数字右移一位继续判断
        }
        return Result.success(count);
    }
}
