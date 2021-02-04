package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hobo
 * @create 2021-02-04 22:39
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ProductFeignClient productFeignClient;


    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        //获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        //获取sku下的一二三级分类name
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        //获取sku下的价格
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuInfo.getId());
        //根据spuId 查询spu的销售属性和销售属性值
        //然后根据skuId查询sku商品是否有销售属性值
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
        //根据spuId 查询同一个spu下面所有sku  组合sku销售属性值的id
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());

        Map<String, Object> result = new HashMap<>();
        result.put("skuInfo", skuInfo);
        result.put("categoryView", categoryView);
        result.put("price", skuPrice);
        result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        result.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));

        return result;
    }
}
