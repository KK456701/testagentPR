package com.study.room.listener;

import com.study.room.config.RabbitMQConfig;
import com.study.room.entity.ReservationOrder;
import com.study.room.service.ReservationOrderService;
import com.study.room.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 专门用于监听“到期前10分钟”延迟消息的消费者
 */
@Component
public class SeatRenewalListener {

    private static final Logger log = LoggerFactory.getLogger(SeatRenewalListener.class);

    @Autowired
    private ReservationOrderService reservationOrderService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFY_QUEUE_NAME)
    public void listenNotifyQueue(Map<String, Object> msg) {
        log.info("【精确预警推送】收到 MQ 延迟通知消息: {}", msg);
        try {
            Long userId = Long.parseLong(String.valueOf(msg.get("userId")));
            Long orderId = Long.parseLong(String.valueOf(msg.get("orderId")));

            log.info("【精确预警推送】时间到了！准备发起到期预警，检查用户 {} 订单 {}", userId, orderId);

            ReservationOrder order = reservationOrderService.getById(orderId);
            if (order != null && order.getPayStatus() != null && order.getPayStatus() == 1) {
                String wMsg = "【温馨提示】您的座位即将于 10分钟后 到期，请注意安排时间！订单号:" + orderId;
                // 事件到达的那一刻（精确到毫秒），调用 WebSocket 推送
                WebSocketServer.sendMessageToUser(String.valueOf(userId), wMsg);
                log.info("【精确预警推送】已经成功调用 WebSocket 发送给用户: {}", userId);
            } else {
                log.info("订单已取消、退款或无效状态，跳过通知。");
            }
        } catch (Exception e) {
            log.error("【精确预警推送】发生致命异常: ", e);
        }
    }
}
