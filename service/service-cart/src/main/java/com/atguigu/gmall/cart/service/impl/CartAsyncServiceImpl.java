package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author Hobo
 * @create 2021-02-26 22:28
 */
@Service
public class CartAsyncServiceImpl implements CartAsyncService {
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Async
    @Override
    public void updateCartInfo(CartInfo cartInfo) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        //根据userId 和skuId 复合主键
        cartInfoQueryWrapper.eq("user_id", cartInfo.getUserId()).eq("sku_id", cartInfo.getSkuId());
        cartInfoMapper.update(cartInfo, cartInfoQueryWrapper);
    }

    @Async
    @Override
    public void saveCartInfo(CartInfo cartInfo) {
        cartInfoMapper.insert(cartInfo);
    }
}
