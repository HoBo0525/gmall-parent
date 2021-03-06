package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Hobo
 * @create 2021-02-04 22:39
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        //创建异步对象
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                //获取skuInfo
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                result.put("skuInfo", skuInfo);
                return skuInfo;
            }
        },threadPoolExecutor);

        CompletableFuture<Void> categoryViewFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //获取sku下的一二三级分类name
                BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                result.put("categoryView", categoryView);
            }
        },threadPoolExecutor);

        CompletableFuture<Void> priceFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //获取sku下的价格
                BigDecimal skuPrice = productFeignClient.getSkuPrice(skuInfo.getId());
                result.put("price", skuPrice);
            }
        },threadPoolExecutor);

        CompletableFuture<Void> spuSaleAttrListFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //根据spuId 查询spu的销售属性和销售属性值
                //然后根据skuId查询sku商品是否有销售属性值
                List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
                result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
            }
        },threadPoolExecutor);

        CompletableFuture<Void> valuesSkuJsonFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                //根据spuId 查询同一个spu下面所有sku  组合sku销售属性值的id
                Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
                result.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
            }
        },threadPoolExecutor);

        CompletableFuture<Void> incrHotScoreCompletableFuture  = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                //更新商品热度
                listFeignClient.incrHotScore(skuId);
            }
        }, threadPoolExecutor);


        //多任务组合
        CompletableFuture.allOf(
                skuInfoFuture,
                categoryViewFuture,
                priceFuture,
                spuSaleAttrListFuture,
                valuesSkuJsonFuture,
                incrHotScoreCompletableFuture).join();

        return result;
    }
}
