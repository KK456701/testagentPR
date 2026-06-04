-- -----------------------------------------------------
-- 沉浸式自习室预约系统 核心表结构 DDL
-- -----------------------------------------------------

-- 如果数据库存在则删除，开发环境可配置
CREATE DATABASE IF NOT EXISTS `study_room` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `study_room`;

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '唯一主键（分布式或自增）',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `password` varchar(128) NOT NULL COMMENT '加密后的密码',
  `nickname` varchar(32) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT '' COMMENT '头像',
  `status` tinyint(1) DEFAULT '1' COMMENT '账号状态 (1正常, 0封禁)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户核心表';

-- 插入一个测试用户 13800138000 , 密码 123456 (假设未加密)
INSERT INTO `user` (`phone`, `password`, `nickname`) VALUES ('13800138000', 'e10adc3949ba59abbe56e057f20f883e', '自习狂魔01');

-- 2. 门店表 (Shop)
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `name` varchar(64) NOT NULL COMMENT '自习室名称',
  `address` varchar(255) NOT NULL COMMENT '自习室详细地址',
  `longitude` decimal(10,6) NOT NULL COMMENT '经度 (供 GEO 计算附近)',
  `latitude` decimal(10,6) NOT NULL COMMENT '纬度',
  `images` varchar(1024) DEFAULT NULL COMMENT '门店图片，逗号分隔URL',
  `open_time` varchar(20) DEFAULT '08:00' COMMENT '早开门时间',
  `close_time` varchar(20) DEFAULT '23:00' COMMENT '晚关门时间',
  `status` tinyint(1) DEFAULT '1' COMMENT '营业状态 (1营业, 0休息)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室门店表';

-- 插入一条测试数据
INSERT INTO `shop` (`name`, `address`, `longitude`, `latitude`, `images`) VALUES ('不挂科自习室(大学城店)', '高教园区商业街3楼', '120.123456', '30.123456', 'http://img/1.png');

-- 3. 座位表 (Seat) - 关联门店
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '座位ID',
  `shop_id` bigint(20) NOT NULL COMMENT '所属门店ID (外键关联shop)',
  `seat_no` varchar(32) NOT NULL COMMENT '内部座位编号 (例如 A区01)',
  `features` varchar(128) DEFAULT NULL COMMENT '座位属性标识(靠窗,大桌,带插座)',
  `price_per_hour` decimal(10,2) NOT NULL COMMENT '每小时计费金额',
  `status` tinyint(1) DEFAULT '1' COMMENT '座位状态 (1可用, 0维修中)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室座位详情表';

-- 插入测试数据
INSERT INTO `seat` (`shop_id`, `seat_no`, `features`, `price_per_hour`) VALUES (1, 'A-01', '靠窗,有插座', 5.00);
INSERT INTO `seat` (`shop_id`, `seat_no`, `features`, `price_per_hour`) VALUES (1, 'A-02', '普通座', 4.00);

-- 4. 预约订单流水表 (Reservation Order) - 核心并发冲突表
-- 记录用户针对某个座位的占用生命周期。
DROP TABLE IF EXISTS `reservation_order`;
CREATE TABLE `reservation_order` (
  `id` bigint(20) NOT NULL COMMENT '分布式业务主键（不走自增，避免分库分表问题）',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `shop_id` bigint(20) NOT NULL COMMENT '冗余：门店ID',
  `seat_id` bigint(20) NOT NULL COMMENT '核心：占用的座位ID',
  `target_date` date NOT NULL COMMENT '预约的日期 (例如 2026-04-05)',
  `start_time` time NOT NULL COMMENT '开始时间段 (例如 08:00:00)',
  `end_time` time NOT NULL COMMENT '结束时间段 (例如 12:00:00)',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_status` tinyint(1) DEFAULT '0' COMMENT '支付状态: 0待支付, 1已支付, 2已取消/超时退款',
  `use_status` tinyint(1) DEFAULT '0' COMMENT '使用状态: 0待使用, 1使用中, 2已结束',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建日期',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  -- 同一天同一个座位只能被预约一个订单，此约束可以作为一个保底机制。
  -- 注意：我们会在Redis层+Lua做高并发的第一层拦截，DB层为第二层拦截
  KEY `idx_seat_date_pay` (`seat_id`, `target_date`, `pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室座位预约订单记录表';