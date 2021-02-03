package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Hobo
 * @create 2021-02-02 22:46
 */
@Api(tags = "sku商品接口")
@RestController
@RequestMapping("admin/product")
public class SkuManageController {

    @Autowired
    ManageService manageService;

    @ApiOperation("根据spuId获取图片列表")
    @GetMapping("spuImageList/{spuId}")
    public Result spuImageListById(@PathVariable Long spuId){
       List<SpuImage> spuImageList = manageService.getSpuImageListById(spuId);
        return Result.ok(spuImageList);
    }

    @ApiOperation("根据spuId获取销售属性")
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable Long spuId){
       List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
       return Result.ok(spuSaleAttrList);
    }


    //http://api.gmall.com/admin/product/saveSkuInfo
    @ApiOperation("添加sku商品")
    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    //http://api.gmall.com/admin/product/list/{page}/{limit}
    @ApiOperation("获取sku分页列表")
    @GetMapping("list/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit){
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> skuInfoIPage = manageService.list(skuInfoPage);
        return Result.ok(skuInfoIPage);
    }

    //http://api.gmall.com/admin/product/onSale/{skuId}
    @ApiOperation("上架sku商品")
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable Long skuId){
        manageService.onSale(skuId);
        return Result.ok();
    }

    //http://api.gmall.com/admin/product/cancelSale/{skuId}
    @ApiOperation("下架sku商品")
    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable Long skuId){
        manageService.cancelSale(skuId);
        return Result.ok();
    }
}
