package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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


    /**
     * 保存平台商品属性及商品属性值
     * @param baseAttrInfo  前台接收的JSON
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据id查询AttrInfo 和 单个AttrInfo 对应的多个AttrInfoValue
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(Long attrId);

    /**
     * 获取Spu商品分页列表
     * @param pageParam 分页参数
     * @param spuInfo   spu参数
     * @return
     */
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo);

    /**
     * 获取所有销售属性
     * @return
     */
    List<BaseSaleAttr> baseSaleAttrList();

    /**
     * 保存spu商品
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuId获取图片列表
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageListById(Long spuId);

    /**
     * 根据spuId获取销售属性与销售属性值
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    /**
     * 添加sku商品
     * @param skuInfo 商品属性
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 获取sku分页列表
     * @param pageParam 分页属性
     * @return
     */
    IPage<SkuInfo> list(Page<SkuInfo> pageParam);

    /**
     * 根据skuId 更改上架状态
     * @param skuId
     */
    void onSale(Long skuId);

    /**
     * 根据skuId 更改上架状态
     * @param skuId
     */
    void cancelSale(Long skuId);

    /**
     * 根据skuId 查询skuInfo以及skuImageList
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据category3Id 查询视图所有信息
     * @param category3Id
     * @return
     */
    BaseCategoryView getCategoryView(Long category3Id);

    /**
     * 根据skuId查询skuInfo中的价格
     * @param skuId
     * @return
     */
    BigDecimal getSkuPrice(Long skuId);

    /**
     * 先根据spuId 查询spu商品的销售属性和销售属性值， 再根据skuId查询sku商品对应的销售属性值
     * @param skuId
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 根据spuId 查询同一个spu下的sku所有销售属性值 然后把sku的销售属性值id组合   spuId =25 {"106|110":40,"107|110":41}
     * @param spuId
     * @return
     */
    Map<Object, Object> getSkuValueIdsMap(Long spuId);

    /**
     * 获取全部分类信息
     */
    List<JSONObject> getBaseCategoryList();
}
