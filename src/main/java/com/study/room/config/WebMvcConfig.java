package com.study.room.config;

import com.study.room.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 拦截所有相关接口
                .addPathPatterns("/**")
                // 排除登录接口和 WebSocket 接口（一般 WebSocket token 会从 URL 或头携带解析）
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/ws/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources",
                        "/v3/api-docs/**"
                );
    }
}
