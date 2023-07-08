package com.lagou.goods.feign;

import com.lagou.entity.Result;
import com.lagou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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
    public Result<Sku> findById(@PathVariable String id);


    /**
     * 库存变更 & 销量变更
     *
     * @param username
     * @return
     */
    @PostMapping("/changeCount")
    public Result changeInventoryAndSaleNumber(@RequestParam(value = "username") String username);

    /**
     * 库存恢复
     * @param skuId
     * @param num
     */
    @PostMapping("/resumeStockNum")
    public Result resumeStockNum(@RequestParam String skuId, @RequestParam Integer num);
}
