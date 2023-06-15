package com.lagou.service.impl;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.Result;
import com.lagou.feign.SkuFeign;
import com.lagou.goods.pojo.Sku;
import com.lagou.mapper.SearchMapper;
import com.lagou.pojo.SkuInfo;
import com.lagou.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cck
 * @date 2023/6/8 10:53
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private SearchMapper searchMapper;

    @Autowired
    private SkuFeign skuFeign;

    /**
     *  商品搜索
     * @param paramMap
     * @return
     */
    @Override
    public Map search(Map<String, String> paramMap) {
        if (paramMap == null){
            return null;
        }
        // 定义返回结果集
        Map<String,Object> resultMap = new HashMap<>();
        //获取查询关键词
        String keyWord = paramMap.get("keyWord");
        //组合条件对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //条件: 关键词
        boolQuery.must(QueryBuilders.matchQuery("name",keyWord).operator(Operator.AND));
        //1条件: 品牌过滤
        if (!StringUtils.isEmpty(paramMap.get("brand"))) {
            boolQuery.filter(QueryBuilders.termQuery("brandName", paramMap.get("brand")));
        }
        //2条件: 规格过滤
        for (String key : paramMap.keySet()) {
            if (key.startsWith("spec_")) {
                String value = paramMap.get(key).replace("%2B", "+");
                boolQuery.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword",value));
            }
        }
        //3:条件: 价格过滤 0-500元 500-1000元 1000-1500元 1500-2000元 2000-2500元 3000元以上
        String price = paramMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            price = price.replace("元","").replace("以上","");
            String[] p = price.split("-");
            if (p.length>0){
                boolQuery.filter(QueryBuilders.rangeQuery("price").gte(p[0]));
                if (p.length == 2) {
                    boolQuery.filter(QueryBuilders.rangeQuery("price").lte(p[1]));
                }
            }
        }


        //1.构建查询条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQuery);

        // paixu
        if (!StringUtils.isEmpty(paramMap.get("sortField"))) {
            if ("ASC".equals(paramMap.get("sortRule"))) {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(
                        paramMap.get("sortField")).order(SortOrder.ASC));
            } else {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(
                        paramMap.get("sortField")).order(SortOrder.DESC));
            }
        }

        // 设置分页
        String pageNum = paramMap.get("pageNum");
        if (null == pageNum) {
            pageNum = "1";
        }
        nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1, 5));

        // 添加品牌(分组)查询
        String skuBrand = "skuBrand";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));
        // 添加规格(规格)聚合
        String skuSpec = "skuSpec";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword").size(10000));
        // 设置高亮域
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        //指定前缀和后缀
        field.preTags("<span style='color:red>'");
        field.postTags("</span>");
        nativeSearchQueryBuilder.withHighlightFields(field);


        //2.执行查询
        AggregatedPage<SkuInfo> aggregatedPage = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class,
                new SearchResultMapper() {
                    /**
                     * 将高亮的数据 替换 非高亮的数据
                     * @param searchResponse  返回的结果集
                     * @param aClass  返回的类型
                     * @param pageable 分页对象
                     * @param <T>
                     * @return
                     */
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                        // 获得结果数据集
                        SearchHits hits = searchResponse.getHits();
                        ArrayList<T> list = new ArrayList<>();
                        for (SearchHit searchHit : hits) {
                            // 没有高亮的数据，需要获得高亮数据，替换skuinfo中没有高亮的数据
                            SkuInfo skuInfo = JSON.parseObject(searchHit.getSourceAsString(), SkuInfo.class);
                            // 获取高亮的数据
                            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                            if (highlightFields!= null && highlightFields.size()>0){
                                HighlightField highlightField = highlightFields.get("name");
                                if (highlightField !=null){
                                    // 取出高亮数据
                                    Text[] fragments = highlightField.getFragments();
                                    StringBuffer stringBuffer = new StringBuffer();
                                    for (Text text : fragments) {
                                        stringBuffer.append(text);
                                    }
                                    // 替换
                                    skuInfo.setName(stringBuffer.toString());
                                    list.add((T)skuInfo);
                                }
                            }

                        }
                        return new AggregatedPageImpl<>(list,pageable, hits.getTotalHits(),searchResponse.getAggregations());
                    }
                });
        //3.从返回结果中获得信息
        resultMap.put("rows",aggregatedPage.getContent()) ;
        resultMap.put("total",aggregatedPage.getTotalElements()) ;
        resultMap.put("totalPages",aggregatedPage.getTotalPages()) ;
        // 获取品牌聚合结果
        StringTerms brandTerms = (StringTerms) aggregatedPage.getAggregation(skuBrand);
        List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("brandList", brandList);
        // 获取规格聚合结果
        StringTerms specTerms = (StringTerms) aggregatedPage.getAggregation(skuSpec);
        List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        // 定义规格的返回结果
        Map<String, Set<String>> specMap = specList(specList);
        resultMap.put("specList",specMap);
        return resultMap;
    }

    private Map<String, Set<String>> specList(List<String> specList) {
        Map<String, Set<String>> specMap = new HashMap<>();
        for (String spec : specList) {
            // 将JSON转换为map
            Map<String,String> map = JSON.parseObject(spec, Map.class);
            // 遍历map
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                Set<String> specValues = specMap.get(key);
                if (specValues == null){
                    specValues = new HashSet<>();
                }
                specValues.add(value);
                specMap.put(key,specValues);
            }
        }
        return specMap;
    }

    @Override
    public void createIndexAndMapping() {
        //创建索引
        esTemplate.createIndex(SkuInfo.class);
        //创建映射
        esTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importAll() {
        HashMap<Object, Object> paramMap = new HashMap<>();
        paramMap.put("status", "1");
        // 远程调用goods微服务，获取所有商品信息
        Result result = skuFeign.findList(paramMap);
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        // 将商品信息导入索引库
        searchMapper.saveAll(skuInfos);
    }

    @Override
    public void deletDataFromEs(String spuId) {
        // 通过spuId查询商品
        List<Sku> list = skuFeign.findListBySpuId(spuId);
        System.out.println("下架商品的数量=="+list.size());
        // 转换
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(list), SkuInfo.class);
        // 设置规格
        for (SkuInfo skuInfo : skuInfos) {
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.deleteAll(skuInfos);
    }
}
