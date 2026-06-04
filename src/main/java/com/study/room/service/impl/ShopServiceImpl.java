package com.study.room.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.room.entity.Shop;
import com.study.room.mapper.ShopMapper;
import com.study.room.service.ShopService;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {}
