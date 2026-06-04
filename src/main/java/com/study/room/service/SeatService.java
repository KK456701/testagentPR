package com.study.room.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.room.common.Result;
import com.study.room.entity.Seat;

public interface SeatService extends IService<Seat> {
    Result<String> bookSeat(Long userId, Long shopId, Long seatId, String targetDate, int startSlot, int endSlot);
}
