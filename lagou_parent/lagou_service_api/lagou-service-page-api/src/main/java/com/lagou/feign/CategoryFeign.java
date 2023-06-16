package com.lagou.feign;

import com.lagou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cck
 * @date 2023/6/16 9:35
 */
@FeignClient("goods")
@RequestMapping("/category")
public interface CategoryFeign {

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable(name = "id") Integer id);
}
