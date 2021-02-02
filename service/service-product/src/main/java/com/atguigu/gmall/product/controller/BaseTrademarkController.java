package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Hobo
 * @create 2021-02-02 19:26
 */
@Api(tags = "商品品牌接口")
@RestController
@RequestMapping("admin/product/baseTrademark")
public class BaseTrademarkController {
    @Autowired
    BaseTrademarkService baseTrademarkService;

    @ApiOperation("查询品牌分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit){
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page, limit);
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkService.selectPage(baseTrademarkPage);
        return  Result.ok(baseTrademarkIPage);
    }

    @ApiOperation("新增品牌")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    @ApiOperation("修改品牌")
    @PutMapping("update")
    public Result update(@RequestBody BaseTrademark baseTrademark){
        baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    @ApiOperation("删除品牌")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    @ApiOperation("根据Id获取品牌")
    @GetMapping("get/{id}")
    public Result getById(@PathVariable Long id){
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }


}
