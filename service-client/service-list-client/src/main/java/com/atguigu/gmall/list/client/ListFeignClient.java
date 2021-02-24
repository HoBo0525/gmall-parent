package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListFeignClientImpl;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Hobo
 * @create 2021-02-11 12:33
 */
@FeignClient(value = "service-list", fallback = ListFeignClientImpl.class)
public interface ListFeignClient {

    /**
     * 商品热度
     * @param skuId
     * @return
     */
    @GetMapping("api/list/inner/incrHotScore/{skuId}")
    Result incrHotScore(@PathVariable Long skuId);

    /**
     * 搜索商品
     * @param listParam
     * @return
     */
    @PostMapping("/api/list")
    Result list(@RequestBody SearchParam listParam);

    /**
     * 上架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/upperGoods/{skuId}")
    Result upperGoods(@PathVariable("skuId") Long skuId);

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("/api/list/inner/lowerGoods/{skuId}")
    Result lowerGoods(@PathVariable("skuId") Long skuId);

}
