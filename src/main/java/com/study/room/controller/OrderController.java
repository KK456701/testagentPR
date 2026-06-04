package com.study.room.controller;

import com.study.room.common.Result;
import com.study.room.config.RabbitMQConfig;
import com.study.room.entity.ReservationOrder;
import com.study.room.service.ReservationOrderService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private ReservationOrderService reservationOrderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 模拟订单支付接口
     */
    @PostMapping("/pay")
    public Result<String> payOrder(@RequestParam Long orderId) {
        ReservationOrder order = reservationOrderService.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        if (order.getPayStatus() != null && order.getPayStatus() != 0) {
            return Result.error("订单状态异常，无法支付");
        }

        // 把 payStatus 改为 1 (已支付)
        order.setPayStatus(1);
        order.setPayTime(java.time.LocalDateTime.now());
        order.setUpdateTime(java.time.LocalDateTime.now());
        
        boolean ok = reservationOrderService.updateById(order);
        if (ok) {
            // 【重点】这里是“精准预警”核心：计算自习结束时间，发送倒计时延迟消息给WebSocket推送模块
            LocalDateTime endDateTime = LocalDateTime.of(order.getTargetDate(), order.getEndTime());
            LocalDateTime notifyTime = endDateTime.minusMinutes(10); // 提前 10 分钟提醒
            
            // 计算当前时间与提醒时间的差值（毫秒）
            long delayMillis = java.time.Duration.between(LocalDateTime.now(), notifyTime).toMillis();
            
            // 为了方便本地测试立刻看到效果，如果算出来的时间已经过了，或者是负数，或者你随便订的，统一强制兜底为 10 秒后提醒
            if (true) {
                delayMillis = 10000; 
            }
            
            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", order.getUserId());
            msg.put("orderId", orderId);
            
            // 发送提醒专属的延迟消息
            long finalDelayMillis = delayMillis;
            rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_EXCHANGE_NAME, RabbitMQConfig.NOTIFY_ROUTING_KEY, msg, message -> {
                message.getMessageProperties().setDelayLong(finalDelayMillis);
                return message;
            });

            return Result.success("支付成功！座位预约生效。系统将在到期前 10 分钟给您发送精确提醒。");
        }
        return Result.error("支付失败，请重试");
    }
}