package com.lagou.mapper;

import com.lagou.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author cck
 * @date 2023/6/8 11:29
 */
public interface SearchMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
