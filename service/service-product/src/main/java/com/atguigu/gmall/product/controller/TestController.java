package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hobo
 * @create 2021-02-06 20:30
 */
@Api(tags = "测试接口")
@RestController
@RequestMapping("admin/product/test")
public class TestController {

    @Autowired
    TestService testService;

    @ApiOperation("lock测试")
    @GetMapping("testLock")
    public Result testLock(){
        testService.testLock();
        return Result.ok();
    }
}
