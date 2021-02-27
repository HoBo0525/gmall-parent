package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author Hobo
 * @create 2021-02-26 21:17
 */

public interface CartInfoService {

    /**
     * 向购物车添加商品
     * @param userId  用户id
     * @param skuId     商品id
     * @param skuNum       添加数量
     */
    void addToCart(Long skuId, String userId, Integer skuNum);

    /**
     * 根据用户Id 或者临时Id  查询购物车信息
     * @param userId    真正得用户Id
     * @param userTempId    临时用户Id
     * @return
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     * 重载
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

}
