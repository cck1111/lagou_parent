package com.lagou.order.feign;

import com.lagou.entity.Result;
import com.lagou.order.pojo.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cck
 * @date 2023/7/4 15:25
 */
@FeignClient(name = "order")
@RequestMapping("/order")
public interface OrderFeign {

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Order> findById(@PathVariable String id);
}
