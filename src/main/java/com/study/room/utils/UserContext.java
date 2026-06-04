package com.study.room.utils;

public class UserContext {
    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setUser(Long userId) {
        THREAD_LOCAL.set(userId);
    }

    public static Long getUser() {
        return THREAD_LOCAL.get();
    }

    public static void removeUser() {
        THREAD_LOCAL.remove();
    }
    ///asdasdqadq545454
    
    // ========== 新增方法 ==========
    /**
     * 调用 JwtUtil 获取 Token 剩余有效时间（毫秒）
     * @param token JWT 字符串
     * @return 剩余毫秒数，若 token 无效或已过期则返回 -1
     */
    public static long getTokenRemainingTime(String token) {
        return JwtUtil.getRemainingTime(token);
    }
}
