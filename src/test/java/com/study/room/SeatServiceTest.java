package com.study.room;

import com.study.room.common.Result;
import com.study.room.service.SeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SeatServiceTest {

    @Autowired
    private SeatService seatService;

    @Test
    void testBookSeatAndDLQ() throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("               🚀 开始测试核心抢座链路              ");
        System.out.println("==================================================");

        Long userId = 1L;
        Long shopId = 100L;
        Long seatId = 888L;
        String targetDate = "2026-04-05";
        int startSlot = 4; // 假设对应早上 10:00
        int endSlot = 8;   // 假设对应中午 12:00

        // 1. 正常用户首次抢座
        Result<String> result1 = seatService.bookSeat(userId, shopId, seatId, targetDate, startSlot, endSlot);
        System.out.println("👤 用户1抢座结果: " + result1.getMsg());

        // 2. 模拟高并发下，另一个用户在同一时间去抢同一个座位（防超卖拦截测试）
        Result<String> result2 = seatService.bookSeat(2L, shopId, seatId, targetDate, startSlot, endSlot);
        System.out.println("👤 用户2（并发抢同座位）结果: " + result2.getMsg());

        System.out.println("\n⏳ 抢座成功，订单已进入 RabbitMQ 延迟队列...");
        System.out.println("⏳ 我们在代码里设置了 TTL 为 15 秒，现在让程序睡眠等待 18 秒...");
        System.out.println("⏳ 请留意下方日志是否出现【死信队列】字样！\n");

        // 3. 让主线程睡 18 秒，等待 RabbitMQ 的 15秒 TTL 到期触发死信队列
        Thread.sleep(18000);

        System.out.println("==================================================");
        System.out.println("               ✅ 测试结束，观察完毕                ");
        System.out.println("==================================================");
    }
}
