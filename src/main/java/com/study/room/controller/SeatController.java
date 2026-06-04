package com.study.room.controller;

import com.study.room.common.Result;
import com.study.room.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    private SeatService seatService;

    // 核心高并发抢座接口
    @PostMapping("/book")
    public Result<String> bookSeat(
            @RequestParam Long userId,
            @RequestParam Long shopId,
            @RequestParam Long seatId,
            @RequestParam String targetDate,
            @RequestParam int startSlot,
            @RequestParam int endSlot) {
        
        // 真正调用 SeatService 的 Lua防超卖与MQ削峰逻辑
        return seatService.bookSeat(userId, shopId, seatId, targetDate, startSlot, endSlot);
    }
}
