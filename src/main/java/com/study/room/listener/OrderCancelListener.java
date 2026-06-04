package com.study.room.listener;

import com.study.room.config.RabbitMQConfig;
import com.study.room.entity.ReservationOrder;
import com.study.room.service.ReservationOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderCancelListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelListener.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ReservationOrderService reservationOrderService;

    @RabbitListener(queues = RabbitMQConfig.DELAY_QUEUE_NAME)
    public void listenDelayQueue(Map<String, Object> msg) {
        log.info("【延迟队列】收到超时订单，检查支付状态准释放库存：{}", msg);

        try {
            Long orderId = Long.parseLong(String.valueOf(msg.get("orderId")));
            Long shopId = Long.parseLong(String.valueOf(msg.get("shopId")));
            Long seatId = Long.parseLong(String.valueOf(msg.get("seatId")));
            String targetDate = String.valueOf(msg.get("targetDate"));
            int startSlot = Integer.parseInt(String.valueOf(msg.get("startSlot")));
            int endSlot = Integer.parseInt(String.valueOf(msg.get("endSlot")));

            ReservationOrder order = reservationOrderService.getById(orderId);
            if (order == null) {
                log.error("订单不存在: {}", orderId);
                return;
            }

            if (order.getPayStatus() != null && order.getPayStatus() == 1) {
                log.info("订单已被支付，无需取消，库存扣减实锤。orderId={}", orderId);
                return;
            }

            order.setPayStatus(2);
            order.setUpdateTime(java.time.LocalDateTime.now());
            reservationOrderService.updateById(order);

            // Redis 释放库存机制：利用 setBit 方法将其还原为 0 (false)
            String key = String.format("seat:booked:%d:%d:%s", shopId, seatId, targetDate);
            for (int i = startSlot; i <= endSlot; i++) {
                final int slot = i;
                stringRedisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Boolean>) connection -> 
                        connection.setBit(key.getBytes(), slot, false)
                );
            }

            log.info("【延迟队列】订单支付超时，座位库存释放完毕! orderId={}, shopId={}, seatId={}, time={}-{}", orderId, shopId, seatId, startSlot, endSlot);
        } catch (Exception e) {
            log.error("【致命异常】延迟消息解析失败或Redis清理失败: ", e);
        }
    }
}
