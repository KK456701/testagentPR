package com.study.room.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ 配置类
 * 基于 x-delayed-message 插件实现延迟取消订单功能
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    public static final String DELAY_EXCHANGE_NAME = "order.delay.exchange";
    public static final String DELAY_QUEUE_NAME = "order.delay.queue";
    public static final String DELAY_ROUTING_KEY = "order.delay.routing.key";

    public static final String NOTIFY_QUEUE_NAME = "order.notify.queue";
    public static final String NOTIFY_ROUTING_KEY = "order.notify.routing.key";

    // 声明开启延迟插件的自定义交换机
    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        // 声明延迟交换机的路由类型（内部具体转发使用的 exchange type）
        args.put("x-delayed-type", "direct");
        // name, type, durable, autoDelete, arguments
        return new CustomExchange(DELAY_EXCHANGE_NAME, "x-delayed-message", true, false, args);
    }

    // 声明接收延迟消息的队列
    @Bean
    public Queue delayQueue() {
        return new Queue(DELAY_QUEUE_NAME, true);
    }

    // 绑定交换机与队列
    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with(DELAY_ROUTING_KEY).noargs();
    }

    // 声明接收【到期提醒】延迟推送消息的队列
    @Bean
    public Queue notifyQueue() {
        return new Queue(NOTIFY_QUEUE_NAME, true);
    }

    // 绑定提醒队列与延迟交换机
    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue()).to(delayExchange()).with(NOTIFY_ROUTING_KEY).noargs();
    }
}
