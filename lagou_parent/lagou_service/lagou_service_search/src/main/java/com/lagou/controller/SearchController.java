package com.lagou.controller;

import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author cck
 * @date 2023/6/8 10:57
 */
@RestController
@RequestMapping("/sku_search")
public class SearchController {

    @Autowired
    private SearchService searchService;


    /**
     *  ES商品搜索功能
     * @param parmMap
     * @return
     */
    @GetMapping("/search")
    public Map search(@RequestParam Map<String,String> parmMap) {
        Map map = searchService.search(parmMap);
        return map;
    }



    @GetMapping("/createIndexAndMapping")
    public Result createIndexAndMapping() {
        searchService.createIndexAndMapping();
        return new Result(true, StatusCode.OK, "创建成功");
    }

    /**
     * 导入所有审核通过的库存数据到ES索引库
     * @return
     */
    @GetMapping("/importAll")
    public Result importAllDataToES() {
        searchService.importAll();
        return new Result(true, StatusCode.OK, "导入数据成 功!");
    }
}
