package com.lagou.listener;

/**
 * @author cck
 * @date 2023/6/16 15:38
 */

import com.lagou.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听消息服务器发送来的消息, 商品id也就是SpuId,
 * 根据这个id, 获取商品数据, 库存集合数据, 分类数据等模板中需要的数据, 然后通过io流生成静态化页面
 * 也就是商品详情页面
 */
@Component
@RabbitListener(queues = "page_create_queue")
public class PageListener {

    @Autowired
    private PageService pageService;
    @RabbitHandler
    public void createPage(String spuId) {
        pageService.createPageHtml(spuId);
    }
}
