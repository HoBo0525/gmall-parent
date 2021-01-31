package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;

import java.util.List;

/**
 * @author Hobo
 * @create 2021-01-31 15:09
 */

public interface ManageService {

    //查询一级分类
    List<BaseCategory1> getCategory1();

    //根据category1_id查询二级分类
    List<BaseCategory2> getCategory2(Long category1Id);

    //查询category2_id查询三级分类
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据一二三级分类的Id 查询平台商品属性
     * @param category1Id 一级分类Id
     * @param category2Id 二级分类Id
     * @param category3Id 三级分类Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);


}