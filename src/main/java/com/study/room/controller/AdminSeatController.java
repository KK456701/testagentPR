package com.study.room.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.room.common.Result;
import com.study.room.entity.Seat;
import com.study.room.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/seat")
public class AdminSeatController {

    @Autowired
    private SeatService seatService;

    // 分页查询当前店铺下的座位
    @GetMapping("/list")
    public Result<Page<Seat>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam Long shopId) {
        Page<Seat> seatPage = new Page<>(page, size);
        QueryWrapper<Seat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId);
        
        return Result.success(seatService.page(seatPage, queryWrapper));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Seat seat) {
        seat.setCreateTime(java.time.LocalDateTime.now());
        boolean ok = seatService.save(seat);
        return ok ? Result.success("座位添加成功") : Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody Seat seat) {
        seat.setUpdateTime(java.time.LocalDateTime.now());
        boolean ok = seatService.updateById(seat);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean ok = seatService.removeById(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }
}