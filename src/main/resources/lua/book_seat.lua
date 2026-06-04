-- ---------------------------------------------------------
-- 自习室座位预约防超卖 Lua 脚本
-- ---------------------------------------------------------
-- KEYS[1] : 某门店某座位在特定日期的占用状态位图 (如: seat:booked:{shop_id}:{seat_id}:{date})
-- ARGV[1] : 预约的起始时间片索引 (0 - 47)
-- ARGV[2] : 预约的结束时间片索引 (0 - 47)

local key = KEYS[1]
local start_slot = tonumber(ARGV[1])
local end_slot = tonumber(ARGV[2])

-- 1. 第一阶段：冲突检测
-- 遍历用户想要预约的时间片，检查是否有任何一个时间片已经被占用（值为1）
for i = start_slot, end_slot do
    local is_booked = redis.call('GETBIT', key, i)
    if is_booked == 1 then
        return 0 -- 返回0代表存在时间段冲突，抢座失败（超卖）
    end
end

-- 2. 第二阶段：原子性扣位（占座）
-- 如果全部可用，则将这些时间片全部标记为已占用（设为1）
for i = start_slot, end_slot do
    redis.call('SETBIT', key, i, 1)
end

-- 3. 设置过期时间兜底（例如过期时间设为2天，防止历史数据无限堆积内存）
-- 172800 = 60 * 60 * 24 * 2
redis.call('EXPIRE', key, 172800)

return 1 -- 返回1代表抢座成功
