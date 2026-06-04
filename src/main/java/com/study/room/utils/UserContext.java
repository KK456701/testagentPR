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
}
