package com.lagou.order.service.impl;

import com.lagou.goods.feign.SkuFeign;
import com.lagou.order.dao.OrderItemMapper;
import com.lagou.order.dao.OrderMapper;
import com.lagou.order.pojo.OrderItem;
import com.lagou.order.service.CartService;
import com.lagou.order.service.OrderService;
import com.lagou.order.pojo.Order;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lagou.user.feign.UserFeign;
import com.lagou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private UserFeign userFeign;

    /**
     * 查询全部列表
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * @param order
     */
    @Override
    public void add(Order order){
        //查询购物车列表
        Map cartMap =
                cartService.list(order.getUsername());
        //明细列表
        List<OrderItem> orderItems = (List<OrderItem>) cartMap.get("orderItemList");
        //统计计算
        int totalPrice = 0;
        int totalNum = 0;
        for (OrderItem orderItem : orderItems) {
            //只计算用户勾选的
            if (orderItem.isChecked()) {
                //总金额
                totalPrice += orderItem.getMoney();
                //总数量
                totalNum += orderItem.getNum();
            }
        }
        //订单ID
        order.setId(String.valueOf(idWorker.nextId()));
        //订单商品总数
        order.setTotalNum(totalNum);
        order.setTotalMoney(totalPrice);
        order.setPayMoney(totalPrice);
        //其他订单数据完善
        order.setCreateTime(new Date());
        order.setUpdateTime(order.getCreateTime());
        //0:未评价，1：已评价
        order.setBuyerRate("0");
        //来源，1：WEB
        order.setSourceType("1");
        //0:未完成,1:已完成，2：已退货
        order.setOrderStatus("0");
        //0:未支付，1：已支付，2：支付失败
        order.setPayStatus("0");
        //0:未发货，1：已发货，2：已收货
        order.setConsignStatus("0");
        //保存订单
        orderMapper.insertSelective(order);
        //调用商品微服务，更新库存机器销量的变更
        skuFeign.changeInventoryAndSaleNumber(order.getUsername());
        //增加用户积分
        userFeign.addPoints(10);
        //添加订单明细
        for (OrderItem orderItem : orderItems) {
            //用户勾选的
            if (orderItem.isChecked()) {
                orderItem.setId(String.valueOf(idWorker.nextId()));
                orderItem.setIsReturn("0");
                orderItem.setOrderId(order.getId());
                //保存订单明细
                orderItemMapper.insertSelective(orderItem);
                //清除Redis缓存购物车数据
                cartService.delete(orderItem.getSkuId(), order.getUsername());
            }
        }
    }


    /**
     * 修改
     * @param order
     */
    @Override
    public void update(Order order){
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap){
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size){
        PageHelper.startPage(page,size);
        return (Page<Order>)orderMapper.selectAll();
    }

    /**
     * 条件+分页查询
     * @param searchMap 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public Page<Order> findPage(Map<String,Object> searchMap, int page, int size){
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        return (Page<Order>)orderMapper.selectByExample(example);
    }

    /**
     * 构建查询对象
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andEqualTo("id",searchMap.get("id"));
           	}
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andEqualTo("payType",searchMap.get("payType"));
           	}
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
           	}
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
           	}
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
           	}
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
           	}
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
           	}
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
           	}
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
           	}
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
           	}
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andEqualTo("sourceType",searchMap.get("sourceType"));
           	}
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
           	}
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andEqualTo("orderStatus",searchMap.get("orderStatus"));
           	}
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andEqualTo("payStatus",searchMap.get("payStatus"));
           	}
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andEqualTo("consignStatus",searchMap.get("consignStatus"));
           	}
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andEqualTo("isDelete",searchMap.get("isDelete"));
           	}

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
