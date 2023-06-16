package com.lagou.feign;

import com.lagou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author cck
 * @date 2023/6/16 9:49
 */
@FeignClient("goods")
@RequestMapping("/sku")
public interface SkuFeign {


    /**
     * 根据商品的id搜索
     * @param spuId
     * @return
     */
    @GetMapping("/findSkuListBySpuId/{spuId}")
    public List<Sku> findListBySpuId(@PathVariable String spuId);
}
