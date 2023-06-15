package com.lagou.listener;

import com.lagou.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cck
 * @date 2023/6/8 17:26
 */
@Component
@RabbitListener(queues = "search_pull_queue")
public class SpuPullListener {

    @Autowired
    private SearchService searchService;

    @RabbitHandler
    public void pullDateFromEs(String spuId){
        System.out.println("接受到需要下架商品id:"+ spuId);
        // 通过id查询skuList从索引库中删除需要的商品
        searchService.deletDataFromEs(spuId);
    }
}
