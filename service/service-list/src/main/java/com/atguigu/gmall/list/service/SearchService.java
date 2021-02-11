package com.atguigu.gmall.list.service;

/**
 * @author Hobo
 * @create 2021-02-11 0:39
 */

public interface SearchService {

    /**
     * 上架商品
     */
    void upperGoods(Long skuId);

    /**
     * 下架商品
     */
    void lowerGoods(Long skuId);

    /**
     * 商品热度
     */
    void incrHotScore(Long skuId);
}
