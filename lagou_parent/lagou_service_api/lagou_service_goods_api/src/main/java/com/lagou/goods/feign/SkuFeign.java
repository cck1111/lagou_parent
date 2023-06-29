package com.lagou.goods.feign;

import com.lagou.entity.Result;
import com.lagou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cck
 * @date 2023/6/29 10:59
 */
@FeignClient(name = "goods")
@RequestMapping("sku")
public interface SkuFeign {


    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Sku>  findById(@PathVariable String id);
}
