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

    /**
     * 修改购物车商品选中状态
     */
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除购物车商品
     */
    void deleteCart(Long skuId, String userId);

    /**
     * 根据用户Id 得到购物车选中的商品信息
     */
    List<CartInfo> getCartCheckedList(String userId);

    /**
     * 更新商品实时价格 并存入缓存
     */
    List<CartInfo> loadCartCache(String userId);
}
