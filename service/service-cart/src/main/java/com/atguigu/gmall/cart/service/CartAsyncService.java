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
}
