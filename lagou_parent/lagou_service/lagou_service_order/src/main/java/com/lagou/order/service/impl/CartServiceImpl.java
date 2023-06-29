package com.lagou.order.service.impl;

import com.lagou.entity.Result;
import com.lagou.goods.feign.SkuFeign;
import com.lagou.goods.feign.SpuFeign;
import com.lagou.goods.pojo.Sku;
import com.lagou.goods.pojo.Spu;
import com.lagou.order.pojo.OrderItem;
import com.lagou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author cck
 * @date 2023/6/29 11:20
 */
@Service
public class CartServiceImpl implements CartService {

    private static final String CART = "Cart_";
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;


    @Override
    public void add(Integer num, Long id, String username) {
        //删除购物车数据
        if (num <= 0) {
            redisTemplate.boundHashOps(CART + username).delete(id.toString());
            return;
        }
        OrderItem orderItem = (OrderItem)
                redisTemplate.boundHashOps(CART + username).get(id.toString());
        if (orderItem != null) {
            //存在，刷新购物车
            orderItem.setNum(orderItem.getNum() + num);
            //如果本商品在购物车中数量为0则从购物车中删除
            if (orderItem.getNum() <= 0) {
                redisTemplate.boundHashOps(CART + username).delete(id.toString());
                return;
            }
            orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());
            orderItem.setPayMoney(orderItem.getNum() * orderItem.getPrice());
        } else {
            //不存在，新增购物车
            Result<Sku> skuResult = skuFeign.findById(id.toString());
            Sku sku = skuResult.getData();
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //将SKU转换成OrderItem
            orderItem = this.skuToOrderItem(sku, spu, num);
        }
        //存入redis
        redisTemplate.boundHashOps(CART + username).put(id.toString(), orderItem);

    }

    /**
     * 获取购物车列表数据
     * @param username
     * @return
     */
    @Override
    public Map list(String username) {
        Map map = new HashMap();
        List<OrderItem> orderItemList = redisTemplate.boundHashOps(CART + username).values();
        map.put("orderItemList", orderItemList);
        //商品数量与总价格
        Integer totalNum = 0;
        Integer totalPrice = 0;
        for (OrderItem orderItem : orderItemList) {
            totalNum += orderItem.getNum();
            totalPrice += orderItem.getMoney();
        }
        map.put("totalNum", totalNum);
        map.put("totalPrice", totalPrice);
        return map;
    }

    /**
     * 删除购物车
     * @param skuId
     * @param userName
     */
    @Override
    public void delete(String skuId, String userName) {
        redisTemplate.opsForHash().delete(CART + userName, skuId);
    }

    /**
     * 购物车商品是否选中
     * @param skuId 库存id
     * @param checked 是否选中
     */
    @Override
    public void updateChecked(String skuId, Boolean checked, String username) {
        Set keys = redisTemplate.boundHashOps(CART +
                username).keys();
        for (Object key : keys) {
            if(key.equals(skuId)){
                OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + username).get(key);
                orderItem.setChecked(checked);
                redisTemplate.boundHashOps(CART + username).put(orderItem.getSkuId(),orderItem);
            }
        }
    }

    /***
     * SKU转成OrderItem
     * @param sku
     * @param num
     * @return
     */
    private OrderItem skuToOrderItem(Sku sku,Spu spu,Integer num){
        OrderItem orderItem = new OrderItem();
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        //单价*数量
        orderItem.setPayMoney(num * orderItem.getPrice());
        //实付金额
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight() * num);
        //重量=单个重量*数量
        //分类ID设置
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }
}

