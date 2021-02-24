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

import java.util.ArrayList;
import java.util.List;
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
    public String getItem(@PathVariable Long skuId, Model model) {
        Result<Map> result = itemFeignClient.getItem(skuId);
        model.addAllAttributes(result.getData());

        return "item/index";
    }

    @GetMapping({"/", "index.html"})
    public String index(Model model) {
        Result result = productFeignClient.getBaseCategoryList();
        model.addAttribute("list", result.getData());
        return "index/index";
    }



    public static void main(String[] args) {
//        System.out.println(getList(32));
        int[] nums = {4,3,2,7,8,2,3,1};
//        System.out.println(findDisappearedNumbers(nums));
        System.out.println(findDisappearedNumbers(nums));
    }

////计算杨辉三角行数与对应值
//    public static ArrayList getList(int row) {
//        ArrayList list = new ArrayList(row + 1);
//        long x = 1;
//        for (int i = 1; i <= row + 1; i++) {
//            list.add((int)x);
//            x = x * (row -i + 1) / i;
//        }
//        return list;
//    }

//    public static List<Integer> findDisappearedNumbers(int[] nums) {
////        for (int num : nums) {
////            if (nums[Math.abs(num) - 1] > 0){
////                nums[Math.abs(num) - 1] *= -1;
////            }
////        }
////
////        ArrayList<Integer> list = new ArrayList<>();
////        for (int i = 0; i < nums.length; i++) {
////            if (nums[i] > 0){
////                list.add(i + 1);
////            }
////        }
////        return list;
////    }
////}

    public static List<Integer> findDisappearedNumbers(int[] nums) {
        int n = nums.length;
        for (int num : nums) {
            int x = (num - 1) % n;
            nums[x] += n;
        }
        List<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            if (nums[i] <= n) {
                ret.add(i + 1);
            }
        }
        return ret;
    }
}
