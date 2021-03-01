package com.atguigu.gmall.cart.client;


import com.atguigu.gmall.cart.client.impl.CartFeignClientImpl;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Hobo
 * @create 2021-02-28 14:47
 */
@FeignClient(value = "service-cart", fallback = CartFeignClientImpl.class)
public interface CartFeignClient {
    /**
     * 前端需要的同步方法  剩下的cartController的方法 都用vue的异步调用
     * @param skuId
     * @param skuNum
     * @return
     */
    @PostMapping("api/cart/addToCart/{skuId}/{skuNum}")
    Result addToCart(@PathVariable Long skuId,
                     @PathVariable Integer skuNum);

    /**
     * 发布 通过用户id 获取购物车中勾选商品的接口
     */
    @GetMapping("api/cart/getCartCheckedList/{userId}")
    List<CartInfo> getCartCheckedList(@PathVariable String userId);

    @GetMapping("api/cart/loadCartCache/{userId}")
    Result loadCartCache(@PathVariable("userId") String userId);
}
