package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

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

    /**
     * 搜索列表
     */
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
