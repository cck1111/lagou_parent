package com.lagou.feign;

import com.lagou.entity.Result;
import com.lagou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author cck
 * @date 2023/6/8 11:20
 */
@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap);

    /**
     *  通过spuId查询sku集合
     * @param spuId
     * @return
     */
    @GetMapping(value = "/findListBySkuId/{spuId}" )
    public List<Sku> findListBySpuId(@PathVariable String spuId);
}
