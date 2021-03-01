package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hobo
 * @create 2021-02-28 20:46
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {
    @Autowired
    UserFeignClient userFeignClient;
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    OrderService orderService;

    /**
     * 确认订单
     */
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request){
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //获取用户地址
        List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(userId);
        //获取购物车信息
        List<CartInfo> cartInfoList = cartFeignClient.getCartCheckedList(userId);

        //声明一个集合存储订单明细
        List<OrderDetail> detailList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());

            detailList.add(orderDetail);
        }

        //创建订单信息
        OrderInfo orderInfo = new OrderInfo();
        //计算总金额
        orderInfo.setOrderDetailList(detailList);
        orderInfo.sumTotalAmount();

        //存储信息
        Map<String, Object> map = new HashMap<>();
        map.put("userAddressList", userAddressListByUserId);
        map.put("detailArrayList", detailList);
        map.put("totalNum", detailList.size());
        map.put("totalAmount", orderInfo.getTotalAmount());

        //存储流水号
        String tradeNo = orderService.getTradeNo(userId);
        map.put("tradeNo", tradeNo);

        return Result.ok(map);
    }

    /**
     * 提交订单
     */
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        orderInfo.setUserId(Long.parseLong(userId));

        //得到订单的流水号
        String tradeNo = request.getParameter("tradeNo");
        //验证
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag){
            return Result.fail().message("不能重复提交订单");
        }
        orderService.deleteTradeNo(userId);

        //验证库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            //验证库存
            boolean b = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if (!b){
                return Result.fail().message(orderDetail.getSkuName() + "商品库存不足");
            }
            //验证价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(orderDetail.getSkuId());
            if (orderDetail.getOrderPrice().compareTo(skuPrice) != 0){
                //重新查询价格
                cartFeignClient.loadCartCache(userId);
                return Result.fail().message(orderDetail.getSkuName() + "商品价格有变、请重新下单");
            }
        }


        //验证通过 保存订单
        Long orderId = orderService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }
}
