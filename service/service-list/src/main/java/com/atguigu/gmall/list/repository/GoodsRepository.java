package com.atguigu.gmall.list.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Hobo
 * @create 2021-02-11 0:41
 */

public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
