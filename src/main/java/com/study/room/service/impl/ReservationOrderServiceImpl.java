package com.study.room.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.room.entity.ReservationOrder;
import com.study.room.mapper.ReservationOrderMapper;
import com.study.room.service.ReservationOrderService;
import org.springframework.stereotype.Service;

@Service
public class ReservationOrderServiceImpl extends ServiceImpl<ReservationOrderMapper, ReservationOrder> implements ReservationOrderService {}
