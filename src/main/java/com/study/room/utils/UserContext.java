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

    // ========== 新增方法 1：调用 JwtUtil.createToken ==========
    /**
     * 根据用户ID生成 JWT Token
     * @param userId 用户ID
     * @return JWT 字符串
     */
    public static String generateToken(Long userId) {
        return JwtUtil.createToken(userId);
    }

    // ========== 新增方法 2：调用 JwtUtil.parseToken ==========
    /**
     * 解析 JWT Token，返回 Claims 对象
     * @param token JWT 字符串
     * @return Claims 对象（包含 userId、过期时间等）
     */
    public static Claims parseToken(String token) {
        return JwtUtil.parseToken(token);
    }


}
