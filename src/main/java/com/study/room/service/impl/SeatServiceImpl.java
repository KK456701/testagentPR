package com.study.room.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.room.common.Result;
import com.study.room.config.RabbitMQConfig;
import com.study.room.entity.ReservationOrder;
import com.study.room.entity.Seat;
import com.study.room.mapper.SeatMapper;
import com.study.room.service.ReservationOrderService;
import com.study.room.service.SeatService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ReservationOrderService reservationOrderService;

    // 提前加载好 Lua 脚本
    private static final DefaultRedisScript<Long> BOOK_SEAT_SCRIPT;
    static {
        BOOK_SEAT_SCRIPT = new DefaultRedisScript<>();
        BOOK_SEAT_SCRIPT.setLocation(new ClassPathResource("lua/book_seat.lua"));
        BOOK_SEAT_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result<String> bookSeat(Long userId, Long shopId, Long seatId, String targetDate, int startSlot, int endSlot) {
        // 1. 核心高并发防超卖与抢座逻辑 (Lua + BitMap)
        String key = String.format("seat:booked:%d:%d:%s", shopId, seatId, targetDate);
        
        Long result = stringRedisTemplate.execute(
                BOOK_SEAT_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(startSlot),
                String.valueOf(endSlot)
        );

        // 2. 超卖拦截：如果返回不是1，说明存在时间段重叠，座位已被抢占
        if (result == null || result != 1L) {
            return Result.error("手慢了，该座位此时段已被预约！");
        }

        // 3. 落库：占座成功，直接在 MySQL 中生成一笔未支付的“待支付订单”
        ReservationOrder order = new ReservationOrder();
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setSeatId(seatId);
        // targetDate是String，例如"2026-04-05"，我们简单转换
        order.setTargetDate(java.time.LocalDate.parse(targetDate));
        // startSlot, endSlot简单转换为LocalTime（假设一个Slot 30分钟，0=00:00）
        order.setStartTime(java.time.LocalTime.of(startSlot / 2, (startSlot % 2) * 30));
        order.setEndTime(java.time.LocalTime.of((endSlot + 1) / 2, ((endSlot + 1) % 2) * 30));
        order.setTotalAmount(java.math.BigDecimal.valueOf(10.0)); // 模拟定价10元
        order.setPayStatus(0); // 0: 待支付
        order.setUseStatus(0); // 0: 待使用
        order.setCreateTime(java.time.LocalDateTime.now());
        
        reservationOrderService.save(order);

        // 4. 发送延时消息：订单倒计时15秒（实际业务应为 15分钟），到期后检查支付状态
        Map<String, Object> msg = new HashMap<>();
        msg.put("orderId", order.getId()); // 将生成的订单ID传给延时队列
        msg.put("userId", userId);
        msg.put("shopId", shopId);
        msg.put("seatId", seatId);
        msg.put("targetDate", targetDate);
        msg.put("startSlot", startSlot);
        msg.put("endSlot", endSlot);

        // 投递到延迟交换机（基于 RabbitMQ 延迟插件，设置超时时间，这里设为 15 秒用于测试）
        rabbitTemplate.convertAndSend(RabbitMQConfig.DELAY_EXCHANGE_NAME, RabbitMQConfig.DELAY_ROUTING_KEY, msg, message -> {
            message.getMessageProperties().setDelay(15000); // 15000ms = 15秒
            return message;
        });

        // 返回包含订单ID的对象
        return Result.success("抢座成功，预约创建完成，请在15分钟内完成支付！订单号：" + order.getId());
    }
}
