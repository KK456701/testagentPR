package com.study.room.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 */
@Configuration
public class WebSocketConfig {

    /**
     * 该 Bean 会自动扫描并注册所有包含 @ServerEndpoint 注解的类。
     * 如果你使用内置 tomcat，那么需要这个 bean。
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
