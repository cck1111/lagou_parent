package com.lagou.pay.feign;

import com.lagou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cck
 * @date 2023/7/6 13:08
 */
@FeignClient(name = "pay")
@RequestMapping("/pay")
public interface AlipayFeign {

    /**
     *  手动查询用户的支付结果
     *  alipay.trade.query(统一收单线下交易)
     *
     * @param outTradeNo
     * @return
     * */
    @GetMapping("/queryStatus")
    public String query(@RequestParam String outTradeNo) throws Exception;;


    /**
     *  手动关闭交易
     *  alipay.trade.query(统一收单线下交易)
     *
     * @param orderId
     * @return
     * */
    @RequestMapping("/close")
    public Result close(@RequestParam String orderId) throws Exception;
}
