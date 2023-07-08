package com.lagou.order.listener;

import com.lagou.entity.Result;
import com.lagou.order.service.OrderService;
import com.lagou.pay.feign.AlipayFeign;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cck
 * @date 2023/7/5 18:30
 */
@Component
@RabbitListener(queues ="ordertimeout_queue")
public class OrderTimeOutListener {

    @Autowired
    private AlipayFeign alipayFeign;
    @Autowired
    private OrderService orderService;

    /**
     *  1.预请求下单获取的支付二维码，不扫码的话，交易订单是没有在支付宝服务创建的
     *  2.扫码但不支付，交易已经创建，可执行关闭请求
     * @param orderId
     */
    @RabbitHandler
    public void orderTimeOutHandle(String orderId) throws Exception {
        //1.去支付宝服务器查询该订单的支付状态，只有处于未支付状态（WAIT_BUYER_PAY）才关闭交易
        String tradeStatus = alipayFeign.query(orderId);
        //如果交易已经关闭 || 交易支付成功 || 交易结束，不可退款，那么无需处理
        if ("TRADE_CLOSED".equals(tradeStatus)
                || "TRADE_SUCCESS".equals(tradeStatus)
                || "TRADE_FINISHED".equals(tradeStatus)) {
            return;
        }
        //已经扫码了，但没有支付,在支付宝服务器交易已经创建创建了
        if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
            //2.在支付宝服务器关闭该交易
            Result result = alipayFeign.close(orderId);
            System.out.println(result);
        }
            //3.本地关闭订单&记录订单日志&回滚库存&回滚销量
            orderService.close(orderId);
    }
}
