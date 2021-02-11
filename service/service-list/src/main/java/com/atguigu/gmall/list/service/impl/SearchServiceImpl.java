package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Hobo
 * @create 2021-02-11 0:43
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void upperGoods(Long skuId) {
        //创建goods对象
        Goods goods = new Goods();

        //查询sku
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                goods.setDefaultImg(skuInfo.getSkuDefaultImg());
                goods.setPrice(skuInfo.getPrice().doubleValue());
                goods.setId(skuInfo.getId());
                goods.setTitle(skuInfo.getSkuName());
                goods.setCreateTime(new Date());
                return skuInfo;
            }
        });
//        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
//        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
//        goods.setPrice(skuInfo.getPrice().doubleValue());
//        goods.setId(skuInfo.getId());
//        goods.setTitle(skuInfo.getSkuName());
//        goods.setCreateTime(new Date());

        //查询品牌
        CompletableFuture<Void> trademarkFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
                if (trademark != null) {
                    goods.setTmId(skuInfo.getTmId());
                    goods.setTmName(trademark.getTmName());
                    goods.setTmLogoUrl(trademark.getLogoUrl());
                }
            }
        });
//        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
//        if (trademark != null){
//            goods.setTmId(skuInfo.getTmId());
//            goods.setTmName(trademark.getTmName());
//            goods.setTmLogoUrl(trademark.getLogoUrl());
//
//        }

        //查询分类
        CompletableFuture<Void> baseCategoryViewFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                if (baseCategoryView != null) {
                    goods.setCategory1Id(baseCategoryView.getCategory1Id());
                    goods.setCategory1Name(baseCategoryView.getCategory1Name());
                    goods.setCategory2Id(baseCategoryView.getCategory2Id());
                    goods.setCategory2Name(baseCategoryView.getCategory2Name());
                    goods.setCategory3Id(baseCategoryView.getCategory3Id());
                    goods.setCategory3Name(baseCategoryView.getCategory3Name());
                }
            }
        });
//        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
//        if (baseCategoryView != null){
//            goods.setCategory1Id(baseCategoryView.getCategory1Id());
//            goods.setCategory1Name(baseCategoryView.getCategory1Name());
//            goods.setCategory2Id(baseCategoryView.getCategory2Id());
//            goods.setCategory2Name(baseCategoryView.getCategory2Name());
//            goods.setCategory3Id(baseCategoryView.getCategory3Id());
//            goods.setCategory3Name(baseCategoryView.getCategory3Name());
//        }

        //查询sku的平台属性
        CompletableFuture<Void> attrListFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
                ArrayList<SearchAttr> attrArrayList = new ArrayList<>();
                if (attrList != null) {
                    for (BaseAttrInfo baseAttrInfo : attrList) {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(baseAttrInfo.getId());
                        searchAttr.setAttrName(baseAttrInfo.getAttrName());

                        //获取属性值
                        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                        for (BaseAttrValue baseAttrValue : attrValueList) {
                            searchAttr.setAttrValue(baseAttrValue.getValueName());
                        }
                        attrArrayList.add(searchAttr);
                    }
                }
                goods.setAttrs(attrArrayList);


            }
        });
//        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
//        ArrayList<SearchAttr> attrArrayList = new ArrayList<>();
//        if (attrList != null){
//            for (BaseAttrInfo baseAttrInfo : attrList) {
//                SearchAttr searchAttr = new SearchAttr();
//                searchAttr.setAttrId(baseAttrInfo.getId());
//                searchAttr.setAttrName(baseAttrInfo.getAttrName());
//
//                //获取属性值
//                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//                for (BaseAttrValue baseAttrValue : attrValueList) {
//                    searchAttr.setAttrValue(baseAttrValue.getValueName());
//                }
//                attrArrayList.add(searchAttr);
//            }
//        }
//        goods.setAttrs(attrArrayList);

        CompletableFuture.allOf(skuInfoFuture, trademarkFuture, baseCategoryViewFuture, attrListFuture).join();
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //创建key
        String hotKey = "hotScore";

        //保存数据
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

        if (hotScore % 10 == 0){
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            Goods goods = optionalGoods.get();
            goods.setHotScore(hotScore.longValue());
            goodsRepository.save(goods);
        }

    }
}
