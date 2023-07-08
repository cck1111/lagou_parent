package com.lagou.order.listener;

import com.alibaba.fastjson.JSON;
import com.lagou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author cck
 * @date 2023/7/5 11:31
 */
@Component
@RabbitListener(queues = "order_queue")
public class OrderListener {

    @Autowired
    private OrderService orderService;
    @RabbitHandler
    public void receivePayStatus(String message){
        System.out.println(message);
        Map<String,String> map = JSON.parseObject(message, Map.class);
        orderService.changePayStatus(map);
    }
}
