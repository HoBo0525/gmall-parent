package com.atguigu.gmall.list.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.impl.ListFeignClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    @GetMapping("inner/incrHotScore/{skuId}")
    Result incrHotScore(@PathVariable Long skuId);
}
