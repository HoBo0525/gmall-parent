package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.swing.text.Highlighter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Hobo
 * @create 2021-02-11 0:43
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public void upperGoods(Long skuId) {
        //创建goods对象
        Goods goods = new Goods();

        //查询sku
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                goods.setDefaultImg(skuInfo.getSkuDefaultImg());
                goods.setPrice(skuInfo.getPrice().doubleValue());
                goods.setId(skuInfo.getId());
                goods.setTitle(skuInfo.getSkuName());
                goods.setCreateTime(new Date());
                return skuInfo;
            }
        });
//        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
//        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
//        goods.setPrice(skuInfo.getPrice().doubleValue());
//        goods.setId(skuInfo.getId());
//        goods.setTitle(skuInfo.getSkuName());
//        goods.setCreateTime(new Date());

        //查询品牌
        CompletableFuture<Void> trademarkFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
                if (trademark != null) {
                    goods.setTmId(skuInfo.getTmId());
                    goods.setTmName(trademark.getTmName());
                    goods.setTmLogoUrl(trademark.getLogoUrl());
                }
            }
        });
//        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
//        if (trademark != null){
//            goods.setTmId(skuInfo.getTmId());
//            goods.setTmName(trademark.getTmName());
//            goods.setTmLogoUrl(trademark.getLogoUrl());
//
//        }

        //查询分类
        CompletableFuture<Void> baseCategoryViewFuture = skuInfoFuture.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                if (baseCategoryView != null) {
                    goods.setCategory1Id(baseCategoryView.getCategory1Id());
                    goods.setCategory1Name(baseCategoryView.getCategory1Name());
                    goods.setCategory2Id(baseCategoryView.getCategory2Id());
                    goods.setCategory2Name(baseCategoryView.getCategory2Name());
                    goods.setCategory3Id(baseCategoryView.getCategory3Id());
                    goods.setCategory3Name(baseCategoryView.getCategory3Name());
                }
            }
        });
//        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
//        if (baseCategoryView != null){
//            goods.setCategory1Id(baseCategoryView.getCategory1Id());
//            goods.setCategory1Name(baseCategoryView.getCategory1Name());
//            goods.setCategory2Id(baseCategoryView.getCategory2Id());
//            goods.setCategory2Name(baseCategoryView.getCategory2Name());
//            goods.setCategory3Id(baseCategoryView.getCategory3Id());
//            goods.setCategory3Name(baseCategoryView.getCategory3Name());
//        }

        //查询sku的平台属性
        CompletableFuture<Void> attrListFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
                ArrayList<SearchAttr> attrArrayList = new ArrayList<>();
                if (attrList != null) {
                    for (BaseAttrInfo baseAttrInfo : attrList) {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(baseAttrInfo.getId());
                        searchAttr.setAttrName(baseAttrInfo.getAttrName());

                        //获取属性值
                        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                        for (BaseAttrValue baseAttrValue : attrValueList) {
                            searchAttr.setAttrValue(baseAttrValue.getValueName());
                        }
                        attrArrayList.add(searchAttr);
                    }
                }
                goods.setAttrs(attrArrayList);


            }
        });
//        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
//        ArrayList<SearchAttr> attrArrayList = new ArrayList<>();
//        if (attrList != null){
//            for (BaseAttrInfo baseAttrInfo : attrList) {
//                SearchAttr searchAttr = new SearchAttr();
//                searchAttr.setAttrId(baseAttrInfo.getId());
//                searchAttr.setAttrName(baseAttrInfo.getAttrName());
//
//                //获取属性值
//                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//                for (BaseAttrValue baseAttrValue : attrValueList) {
//                    searchAttr.setAttrValue(baseAttrValue.getValueName());
//                }
//                attrArrayList.add(searchAttr);
//            }
//        }
//        goods.setAttrs(attrArrayList);

        CompletableFuture.allOf(skuInfoFuture, trademarkFuture, baseCategoryViewFuture, attrListFuture).join();
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //创建key
        String hotKey = "hotScore";

        //保存数据
        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

        if (hotScore % 10 == 0){
            Optional<Goods> optionalGoods = goodsRepository.findById(skuId);
            Goods goods = optionalGoods.get();
            goods.setHotScore(hotScore.longValue());
            goodsRepository.save(goods);
        }
        

    }


    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {

        //构建DSL语句
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        //执行DSL语句 返回响应结果
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //把响应结果封装给响应对象 SearchResponseVo
        SearchResponseVo responseVo = this.parseSearchResult(searchResponse);

        //把基本参数封装
        responseVo.setPageSize(searchParam.getPageSize());
        responseVo.setPageNo(searchParam.getPageNo());
        long totalPages = (responseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();
        responseVo.setTotalPages(totalPages);
        return responseVo;
    }

    /**
     * 封装查询结果
     * @param searchResponse  es查询的响应结果
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        /*
        private List<SearchResponseTmVo> trademarkList;
        private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
        private List<Goods> goodsList = new ArrayList<>();
        private Long total;//总记录数
         */
        SearchHits hits = searchResponse.getHits();
        //  赋值品牌集合 需要从聚合中获取
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //  通过map 来获取到对应的数据 Aggregation ---> ParsedLongTerms
        //  为什么需要转换主要是想获取到buckets
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        //  Function 有参数，有返回值
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map((bucket) -> {
            //  什么一个品牌对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //  获取到了品牌Id
            String keyAsString = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(keyAsString));

            //  赋值品牌Name 是在另外一个桶中
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            //  赋值品牌的LogoUrl
            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        //  添加品牌的
        searchResponseVo.setTrademarkList(trademarkList);

        //  添加平台属性 attrAgg 属于nested 类型
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        //  在转完之后在获取attrIdAgg
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        //  获取对应的平台属性数据
        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map((bucket) -> {
            //  什么一个平台属性对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //  获取到平台属性Id
            Number keyAsNumber = ((Terms.Bucket) bucket).getKeyAsNumber();
            searchResponseAttrVo.setAttrId(keyAsNumber.longValue());
            //  获取到平台属性名称
            ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);
            //  获取平台属性值的名称
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            //  平台属性值名称对应有多个数据 ,需要循环遍历获取到里面的每个key 所对应的数据
            List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();

            List<String> vlaues = buckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());

            searchResponseAttrVo.setAttrValueList(vlaues);
            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        searchResponseVo.setAttrsList(attrsList);
        // 商品集合 goodsList
        SearchHit[] subHits = hits.getHits();
        //  声明一个集合来存储Goods
        List<Goods> goodsList = new ArrayList<>();
        //  循环遍历
        for (SearchHit subHit : subHits) {
            //  是一个Goods.class 组成的json 字符串
            String sourceAsString = subHit.getSourceAsString();
            //  将sourceAsString 变为Goods的对象
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //  细节： 如果通过关键词检索，获取到高亮字段
            if(subHit.getHighlightFields().get("title")!=null){
                //  说明你是通过关键词检索的
                Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                //  覆盖原来的title
                goods.setTitle(title.toString());
            }
            goodsList.add(goods);
        }
        //  赋值商品集合对象
        searchResponseVo.setGoodsList(goodsList);
        //  赋值total
        searchResponseVo.setTotal(hits.totalHits);
        return searchResponseVo;
    }


    /**
     * 用JAVA写DSL语句
     * @param searchParam 查询条件参数
     * @return
     */
    private SearchRequest buildQueryDsl(SearchParam searchParam) {
        //制作查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构建query --> bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //根据一级分类Id查询
        if (searchParam.getCategory1Id() != null){
            // query --> bool --> filter --> term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        //根据二级分类Id查询
        if (searchParam.getCategory2Id() != null){
            // query --> bool --> filter --> term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        //根据一级分类Id查询
        if (searchParam.getCategory3Id() != null){
            // query --> bool --> filter --> term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }

        //根据品牌查询
        String trademark = searchParam.getTrademark();
        //trademark=2:华为
        if (!StringUtils.isEmpty(trademark)){
            String[] split = trademark.split(":");
            if (split != null && split.length == 2){
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //根据商品平台属性查询 nested
        //props=23:4G:运行内存
        //平台属性Id 平台属性值名称 平台属性名
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0){
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split != null && split.length == 3){
                    //创建两个bool
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //构建查询条件
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    //  将subBoolQuery 赋值到boolQuery 中
                    boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                    // 将boolQuery放到boolQueryBuilder
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

        //根据关键字查询
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.OR));
        }

        searchSourceBuilder.query(boolQueryBuilder);

        //分页
        int from = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(from);

        //  默认每页显示三条数据
        searchSourceBuilder.size(searchParam.getPageSize());

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //排序    order=1:desc  order=1:asc
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            String[] split = order.split(":");
            if (split != null && split.length == 2){
                String field = "";
                //判断数组第一位
                switch (split[0]){
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                searchSourceBuilder.sort(field, "asc".equals(split[1])? SortOrder.ASC : SortOrder.DESC);
            }
        }

        //聚合
        //品牌聚合
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")));
        searchSourceBuilder.aggregation(nestedAggregationBuilder);

        System.out.println("DSL:" + searchSourceBuilder);

        //设置想要的数据字段 id，defaultImg，title，price ， 其他字段在展示的时候给你设置成null
        searchSourceBuilder.fetchSource(new String[]{"id","defaultImg","title","price"}, null);

        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        SearchRequest source = searchRequest.source(searchSourceBuilder);
        return  source;

    }
}
