package com.lagou.controller;

import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cck
 * @date 2023/6/16 10:01
 */
@RestController
@RequestMapping("/page")
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 生成静态页面
     * @param id spuId
     * @return
     */
    @RequestMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(name="id") String id){
        pageService.createPageHtml(id);
        return new Result(true, StatusCode.OK,"ok");
    }

}
