package com.study.room.interceptor;

import com.study.room.utils.JwtUtil;
import com.study.room.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求放行 (OPTIONS)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头获取 token
        String token = request.getHeader("Authorization");

        // 若没有 token 或格式不符合规范
        if (!StringUtils.hasText(token)) {
            log.warn("请求未携带Token，拦截拦截：{}", request.getRequestURI());
            response.setStatus(401);
            return false;
        }

        try {
            // 解析令牌
            token = token.replace("Bearer ", "");
            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            
            // 存入 ThreadLocal
            UserContext.setUser(userId);
            log.info("用户验权成功, userId: {}", userId);
            
            return true;
        } catch (Exception e) {
            log.error("Token解析失败或已过期: {}", e.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后，必须清理 ThreadLocal 防止内存泄漏及线程池复用造成的脏数据
        UserContext.removeUser();
    }
}
