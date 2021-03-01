package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Hobo
 * @create 2021-03-01 19:23
 */

public interface OrderService extends IService<OrderInfo> {
    /**
     * 保存订单
     */
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * 生产流水号
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     */
    boolean checkTradeCode(String userId, String tradeCodeNo);

    /**
     * 删除redis中的流水号
     */
    void deleteTradeNo(String userId);

    /**
     * 验证库存
     */
    boolean checkStock(Long skuId, Integer skuNum);
}
