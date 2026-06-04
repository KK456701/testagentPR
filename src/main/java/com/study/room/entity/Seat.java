package com.study.room.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seat")
public class Seat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long shopId;
    private String seatNo;
    private String features;
    private BigDecimal pricePerHour;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
