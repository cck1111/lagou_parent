package com.lagou.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 监控td_ad表的数变动
 * @author cck
 * @date 2023/6/6 17:13
 */
@CanalEventListener
public class BusinessListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "lagou_business", table = {"tb_ad"})
    public void adUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        /**
         *  将修改后的数据发送到mq，只需将首页广告的标志位置 position 发送过去
         *  tb_ad表中存储了网站所有的缓存信息，不仅仅是首页广告
         *  只是首页广告的lua脚本，也有家电区编写 缓存更新和缓存加载的lua脚本
         */
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if (column.getName() .equals("position")){
                System.out.println("发送消息到mq ad_update_queue:"+column.getValue());
                rabbitTemplate.convertAndSend("","ad_update_queue",column.getValue());
            }
        }

    }
}
