package com.lagou.service.impl;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.Result;
import com.lagou.feign.CategoryFeign;
import com.lagou.feign.SkuFeign;
import com.lagou.feign.SpuFeign;
import com.lagou.goods.pojo.Sku;
import com.lagou.goods.pojo.Spu;
import com.lagou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cck
 * @date 2023/6/16 10:03
 */
@Service
public class PageServiceImpl implements PageService {


    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private CategoryFeign categoryFeign;
    /**
     *  静态化模板引擎
     */
    @Autowired
    private TemplateEngine templateEngine;
    /**
     *  静态化页面保存路径
     */
    @Value("${pagepath}")
    private String pagepath;


    @Override
    public void createPageHtml(String spuId) {
        // 存储所有生成静态页所需要的数据
        Map<String,Object> resultMap = new HashMap<>();
        // 1.调用商品微服务加载Spu对象,分类，Sku列表
        loadGoodsInfo(spuId, resultMap);
        // 2.在指定位置通过模板引擎生成静态页面
        createStaticPage(spuId, resultMap);
    }

    private void createStaticPage(String spuId, Map<String, Object> resultMap) {
        // 将数据放入上下文中
        Context context = new Context();
        // 在模板中可以通过map的key获取value值
        context.setVariables(resultMap);
        //设置输出目录
        File file = new File(pagepath);
        if (!file.exists()){
            file.mkdir();
        }
        // 同构模板引擎对象生成静态页
        Writer writer = null;
        try {
             writer= new PrintWriter(file + "/" + spuId + ".html");
             templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadGoodsInfo(String spuId, Map<String, Object> resultMap) {
        //1.
        Result result = spuFeign.findById(spuId);
        Spu spu = JSON.parseObject(JSON.toJSONString(result.getData()),Spu.class);
        resultMap.put("spu",spu);
        String images = spu.getImages();
        List<Map> maps = JSON.parseArray(images, Map.class);
        // 2.图片列表
        List<String> imageList = new ArrayList<>();
        if (maps != null && maps.size() >0){
            for (Map map : maps) {
                imageList.add(String.valueOf(map.get("url")));           }
        }
        resultMap.put("imageList",imageList);
        // 3.获得分类
        resultMap.put("category1",categoryFeign.findById(spu.getCategory1Id()).getData());
        resultMap.put("category2",categoryFeign.findById(spu.getCategory2Id()).getData());
        resultMap.put("category3",categoryFeign.findById(spu.getCategory3Id()).getData());
        // 4.根据spuId获得sku集合
        List<Sku> skuList = skuFeign.findListBySpuId(spu.getId());
        resultMap.put("skuList",skuList);
        //5.规格处理
        String specItems = spu.getSpecItems();
        Map mapSpec = JSON.parseObject(specItems, Map.class);
        resultMap.put("specificationList",mapSpec);
    }
}
