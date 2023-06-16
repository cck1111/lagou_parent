package com.lagou.feign;

import com.lagou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cck
 * @date 2023/6/16 9:50
 */
@FeignClient("goods")
@RequestMapping("/spu")
public interface SpuFeign {

    /***
     * 根据SpuID查询Spu信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id);

}
