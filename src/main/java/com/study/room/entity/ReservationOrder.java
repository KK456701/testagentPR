package com.study.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("reservation_order")
public class ReservationOrder {
    // 采用雪花算法或分布式ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long shopId;
    private Long seatId;
    private LocalDate targetDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal totalAmount;
    private Integer payStatus;
    private Integer useStatus;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime updateTime;
}
