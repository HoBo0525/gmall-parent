package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author Hobo
 * @create 2021-02-02 19:32
 */

public interface BaseTrademarkService extends IService<BaseTrademark> {

    /**
     * 获取品牌分页
     * @param pageParam
     * @return
     */
    IPage<BaseTrademark> selectPage (Page<BaseTrademark> pageParam);

}
