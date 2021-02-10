package com.atguigu.gmall.product.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * @author Hobo
 * @create 2021-02-04 20:10
 */
@Api(tags = "渲染页面数据")
@RestController
@RequestMapping("api/product")
public class ProductApiController {

    @Autowired
    ManageService manageService;

    @ApiOperation("根据skuId查询skuInfo与skuImage")
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        return skuInfo;
    }

    @ApiOperation("获取分类信息")
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        BaseCategoryView baseCategoryView = manageService.getCategoryView(category3Id);
        return baseCategoryView;
    }

    @ApiOperation("获取价格信息")
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal  getSkuPrice(@PathVariable Long skuId){
        BigDecimal price = manageService.getSkuPrice(skuId);
        return price;
    }

    @ApiOperation("获取销售信息并锁定销售属性值")
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable Long skuId,
                                                          @PathVariable Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
        return spuSaleAttrList;
    }

    @ApiOperation("获取商品切换属性")
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable Long spuId){
       Map<Object, Object> map = manageService.getSkuValueIdsMap(spuId);
       return map;
    }

    @ApiOperation("首页分类属性")
    @GetMapping("getBaseCategoryList")
    public Result getBaseCategoryList(){
        List<JSONObject> list = manageService.getBaseCategoryList();
        return Result.ok(list);
    }
}
