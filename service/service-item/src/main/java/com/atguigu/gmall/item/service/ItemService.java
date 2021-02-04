package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @author Hobo
 * @create 2021-02-04 22:38
 */

public interface ItemService {

    Map<String, Object> getBySkuId(Long skuId);
}
