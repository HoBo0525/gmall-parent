package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

/**
 * @author Hobo
 * @create 2021-02-28 14:59
 */
@Controller
public class CartController {
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    ProductFeignClient productFeignClient;

    /**
     * 添加商品到购物车的同步方法
     */
    @GetMapping("addCart.html")
    public String addCart(@RequestParam Long skuId,
                          @RequestParam Integer skuNum,
                          HttpServletRequest request){
        cartFeignClient.addToCart(skuId, skuNum);

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);
        return "cart/addCart";
    }

    /**
     * 查看购物车
     */
    @GetMapping("cart.html")
    public String cartList(HttpServletRequest request){
        return "cart/index";
    }

}
