package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

/**
 * @author Hobo
 * @create 2021-02-26 22:27
 */

public interface CartAsyncService {
    /**
     * 修改购物车商品
     */
    void updateCartInfo(CartInfo cartInfo);

    /**
     * 增加购物车商品
     */
    void saveCartInfo(CartInfo cartInfo);

    /**
     * 删除临时购物车得所有商品
     */
    void deleteCartInfo(String userId);

    /**
     * 商品在购物车选中状态变更
     */
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除在购物车的商品
     */
    void deleteCartInfo(String userId, Long skuId);
}
