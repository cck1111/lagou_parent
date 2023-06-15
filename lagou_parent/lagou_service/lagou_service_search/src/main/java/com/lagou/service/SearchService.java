package com.lagou.service;

import java.util.Map;

/**
 * @author cck
 * @date 2023/6/8 10:52
 */
public interface SearchService {

    /**
     * 创建索引库结构
     */
    public void createIndexAndMapping();

    void importAll();


    void deletDataFromEs(String spuId);

    Map search(Map<String, String> parmMap);
}
