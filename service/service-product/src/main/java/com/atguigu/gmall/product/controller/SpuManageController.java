package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "SPU的数据接口")
@RestController
@RequestMapping("admin/product")
public class SpuManageController {

    @Autowired
    private ManageService manageService;


    @ApiOperation("查询spu商品列表")
    @GetMapping("{page}/{limit}")
    public Result getSpuInfoPage(@PathVariable Long page,
                                 @PathVariable Long limit,
                                 @RequestParam SpuInfo spuInfo){
        // 创建一个Page 对象
        Page<SpuInfo> spuInfoPageList = new Page<>(page, limit);
        IPage<SpuInfo> serviceSpuInfoPage = manageService.getSpuInfoPage(spuInfoPageList, spuInfo);
        //  放入查询之后的数据集
        return Result.ok(serviceSpuInfoPage);

    }

}