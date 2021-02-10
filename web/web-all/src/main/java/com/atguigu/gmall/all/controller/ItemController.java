package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author Hobo
 * @create 2021-02-04 23:03
 */
@Controller
public class ItemController {
    @Autowired
    ItemFeignClient itemFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId, Model model){
        Result<Map> result = itemFeignClient.getItem(skuId);
        model.addAllAttributes(result.getData());

        return "item/index";
    }

    @GetMapping({"/", "index.html"})
    public String index(Model model){
        Result result = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", result.getData());
        return "index/index";
    }
}
