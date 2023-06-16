package com.lagou.service;

/**
 * @author cck
 * @date 2023/6/16 10:02
 */
public interface PageService {

    /**
     * 根据商品的ID 生成静态页
     * @param spuId
     */
    void createPageHtml(String spuId);
}
