package com.atguigu.gmall.order.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Hobo
 * @create 2021-03-01 19:13
 */
@Component
public class OrderFeignClientImpl implements OrderFeignClient {
    @Override
    public Result<Map<String, Object>> trade() {
        return null;
    }
}
