package com.study.room.controller;

import com.study.room.common.Result;
import com.study.room.entity.User;
import com.study.room.service.UserService;
import com.study.room.utils.JwtUtil;
import com.study.room.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 简单模拟登录，生成 JWT Token
    @PostMapping("/login")
    public Result<String> login(@RequestParam Long userId) {
        // 在实际开发中这里应有查询数据库进行账号密码比对的逻辑
        // 我们直接根据 userId 模拟成功并发放 jwt
        String token = JwtUtil.createToken(userId);
        return Result.success(token);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        // 在方法内也可以演示 ThreadLocal 取值，看当前请求的执行人是不是被拦截器放入进来的这个了
        // Long currentLoginUser = UserContext.getUser(); 
        return Result.success(userService.getById(id));
    }

    // 每日签到
    @PostMapping("/sign")
    public Result<String> sign(@RequestParam Long userId) {
        return userService.sign(userId);
    }

    // 获取本月连续签到天数
    @GetMapping("/sign/count")
    public Result<Integer> signCount(@RequestParam Long userId) {
        return userService.signCount(userId);
    }
}
