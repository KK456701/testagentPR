package com.study.room.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.room.common.Result;
import com.study.room.entity.Shop;
import com.study.room.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
public class AdminShopController {

    @Autowired
    private ShopService shopService;

    // 分页查询店铺列表
    @GetMapping("/list")
    public Result<Page<Shop>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String name) {
        Page<Shop> shopPage = new Page<>(page, size);
        QueryWrapper<Shop> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        return Result.success(shopService.page(shopPage, queryWrapper));
    }

    // 新增店铺
    @PostMapping("/add")
    public Result<String> add(@RequestBody Shop shop) {
        shop.setCreateTime(java.time.LocalDateTime.now());
        boolean ok = shopService.save(shop);
        return ok ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 修改店铺
    @PutMapping("/update")
    public Result<String> update(@RequestBody Shop shop) {
        shop.setUpdateTime(java.time.LocalDateTime.now());
        boolean ok = shopService.updateById(shop);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 取下/删除店铺 （支持逻辑删除）
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean ok = shopService.removeById(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }
}