package com.lagou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.order.feign.OrderFeign;
import com.lagou.order.pojo.Order;
import com.lagou.pay.config.AlipayConfig;
import com.lagou.pay.util.MatrixToImageWriter;
import com.lagou.seckill.pojo.SeckillOrder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author cck
 * @date 2023/7/4 15:14
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 为了保持接口的幂等性，在前端系统调用该接口之前需要先进行支付的校验
     * 该接口中申请二维码链接之前，先判断支付状态
     * 请求二维码
     * @param orderId  订单ID(out_trade_no)
     * @param exchange  申请二维码的时候区分是普通订单还是秒杀订单  普通订单：order_exchange; 秒杀订单 seckill_exchange
     * @param response
     */
    @RequestMapping("/qrCode")
    public void preCreate(String orderId, String exchange,HttpServletResponse response) throws Exception {
        // 1.获取订单对象，判断支付状态
        Order order = orderFeign.findById(orderId).getData();
        if (order == null){
            response.getOutputStream().print(orderId +"nocunzai");
        }
        if ("1".equals(order.getPayStatus()) ){
            response.getOutputStream().print(orderId +"yizhifu");
        }
        //如果是普通订单去订单微服务获取订单对象,如果是秒杀订单去redis中获取订单对象SeckillOrder
        String totalMoney = null;
        //普通订单
        if ("order_exchange".equals(exchange)) {
            totalMoney = orderFeign.findById(orderId).getData().getTotalMoney().toString();
        }
        //秒杀订单
        if ("seckill_exchange".equals(exchange)) {
            totalMoney = ((SeckillOrder)redisTemplate.boundHashOps("SeckillOrder_").get("yuanjing")).getMoney().toString();
        }

        // 2.创建AlipayTradePrecreateRequest 对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        // 设置notifyUrl
        // 3.创建AlipayTradePrecreateModel
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        //设置商户订单号
        model.setOutTradeNo(orderId);
        //卖家支付宝用户ID
        model.setSellerId("2088721004694751");
        // 设置body 回调的时候会原参数返回(来区分是普通订单 还是 秒杀订单)
        model.setBody(exchange);
        //设置支付金额
        model.setTotalAmount(totalMoney);
        //商品的标题/交易标题/订单标题/订单关键字等。
        model.setSubject("拉勾商城-订单支付");
        /** 销售产品码。*/
        model.setProductCode("FACE_TO_FACE_PAYMENT");
        request.setNotifyUrl("");
        request.setBizModel(model);
        // 4.发出请求，获得二维码链接
        AlipayTradePrecreateResponse response1 = alipayClient.execute(request);
        if (response1.isSuccess() && "10000".equals(response1.getCode())) {
            // 5.通过二维码链接生成收款二维码
            // 获取二维码内容字符串
            final String qrCode = response1.getQrCode();
            System.out.println("response.getQrCode() = " + qrCode);
            QRCodeWriter writer = new QRCodeWriter();
            //绘制二维码
            BitMatrix bt = writer.encode(qrCode,
                    BarcodeFormat.QR_CODE, 300, 300);
            final ServletOutputStream outputStream = response.getOutputStream();
            // 生成二维码，将二维码写到输出流，返回页面
            MatrixToImageWriter.writeToStream(bt, "jpg", outputStream);
        }
    }

    /**
     *  手动查询用户的支付结果
     *  alipay.trade.query(统一收单线下交易)
     *
     * @param outTradeNo
     * @return
     * */
    @GetMapping("/queryStatus")
    public String query(@RequestParam String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        // 设置订单ID
        model.setOutTradeNo(outTradeNo);
        request.setBizModel(model);
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        String code = response.getCode();
        if (response.isSuccess() && "10000".equals(code)) {
            return response.getBody();
        } else {
            String subCode = response.getSubCode();
            if ("ACQ.SYSTEM_ERROR".equals(subCode)) {
                return "系统错误,请重新发起请求";
            }
            if ("ACQ.INVALID_PARAMETER".equals(subCode)) {
                return "参数无效,检查请求参数，修改后重新发起请求";
            }
            if ("ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                return "查询的交易不存在,检查传入的交易号是否正确,请修改后重新发起请求";
            }
        }
        return response.getBody();
    }

    /**
     *  支付回调方法
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("/notify")
    public String notifyUrl(HttpServletRequest request) throws Exception {

        System.out.println("===================异步回调 =======================");
        String result = "";
        //获取支付宝GET过来反馈信息
        Map<String, String> params = new HashMap<String,String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter =
            requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ?
                        valueStr + values[i] :
                        valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用,如果不乱码使用的话可能会导致签名认证失败
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            System.out.println(name + ":" + valueStr);
            params.put(name, valueStr);
        }
        // 调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params,
                        alipayConfig.getAlipay_public_key(),
                        alipayConfig.getCharset(),
                        alipayConfig.getSigntype());
        if (signVerified) {
            System.out.println("支付宝回调签名认证成功");
            //商户订单号
            String out_trade_no = params.get("out_trade_no");
            //支付宝交易号
            String trade_no = params.get("trade_no");
            //付款金额
            String total_amount = params.get("total_amount");
            //卖家付款的时间
            String gmt_payment = params.get("gmt_payment");
            result = "trade_no:" + trade_no +
                    ",out_trade_no:" + out_trade_no + ",total_amount:" +
                    total_amount + ",gmt_payment:" + gmt_payment;
            //发送到MQ
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("out_trade_no", out_trade_no);
            messageMap.put("trade_no", trade_no);
            messageMap.put("total_amount", total_amount);
            messageMap.put("gmt_payment", gmt_payment);
            String jsonString = JSON.toJSONString(messageMap);
            System.out.println(jsonString);
            String exchange = params.get("body");
            //发送到MQ
            rabbitTemplate.convertAndSend(exchange, "", jsonString);
             return "success";
        } else {
            System.out.println("支付宝回调签名认证失败");
            return "fail";
        }
    }

    /**
     *  手动关闭交易
     *  alipay.trade.query(统一收单线下交易)
     *
     * @param orderId
     * @return
     * */
    @RequestMapping("/close")
    public Result close(@RequestParam String orderId) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(orderId);
        //将数据模型放入关闭请求中
        request.setBizModel(model);
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if(response.isSuccess() && "10000".equals(response.getCode())){
            System.out.println(orderId+",交易已关闭");
            return new Result(true,StatusCode.OK,orderId+",成功已关闭");
        } else {
            System.out.println(response.getSubCode()+":"+response.getSubMsg());
            return new Result(false,StatusCode.ERROR,response.getSubCode()+":"+response.getSubMsg());
        }
    }
}
