package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Hobo
 * @create 2021-01-31 17:10
 */
@Api(tags = "商品基础属性接口")
@RestController
@Slf4j
@RequestMapping("admin/product")
@CrossOrigin
public class BaseManageController {
    @Autowired
    ManageService manageService;

    @ApiOperation("查询一级分类")
    @GetMapping("getCategory1")
    public Result<List<BaseCategory1>> getCategory1(){
        List<BaseCategory1> category1List = manageService.getCategory1();
        return Result.ok(category1List);
    }

    @ApiOperation("查询二级分类")
    @GetMapping("getCategory2/{category1Id}")
    public Result<List<BaseCategory2>> getCategory2(@PathVariable Long category1Id){
        List<BaseCategory2> category2List = manageService.getCategory2(category1Id);
        return Result.ok(category2List);
    }

    @ApiOperation("查询三级分类")
    @GetMapping("getCategory3/{category2Id}")
    public Result<List<BaseCategory3>> getCategory3(@PathVariable Long category2Id){
        List<BaseCategory3> category3List = manageService.getCategory3(category2Id);
        return Result.ok(category3List);
    }

    @ApiOperation("查询商品平台属性")
    @GetMapping("getAttrInfo/{category1Id}/{category2Id}/{category3Id}")
    public Result<List<BaseAttrInfo>> getAttrInfo(@PathVariable Long category1Id,
                                                  @PathVariable Long category2Id,
                                                  @PathVariable Long category3Id){
        List<BaseAttrInfo> attrInfoList = manageService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(attrInfoList);
    }

}
