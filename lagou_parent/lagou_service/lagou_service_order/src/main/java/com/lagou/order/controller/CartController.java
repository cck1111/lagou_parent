package com.lagou.order.controller;

import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.order.service.CartService;
import com.lagou.order.util.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author cck
 * @date 2023/6/29 11:06
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/add")
    public Result add(Long id,Integer num){

        //获取令牌中的用户姓名
        String userName = TokenDecode.getUserInfo().get("username");

        //将商品加入购物车
        cartService.add(num,id,userName);
        return new Result(true, StatusCode.OK,"加入购物车成功！");
    }

    /**
     * 查询用户购物车列表
     * @return
     */
    @GetMapping(value = "/list")
    public Map list(){
        //获取令牌中的用户姓名
        String userName =
                TokenDecode.getUserInfo().get("username");

        return cartService.list(userName);
    }

    /**
     * 删除加入购物车中的商品
     * @param skuId
     * @return
     */
    @DeleteMapping("/delete")
    public Result delete(@RequestParam(name = "skuId") String skuId) {
        //获取令牌中的用户姓名
        String userName =
                TokenDecode.getUserInfo().get("username");

        cartService.delete(skuId, userName);
        return new Result(true, StatusCode.OK,"删除成功");
    }

    /**
     * 更新购物车选项 复选框
     */
    @PutMapping("/updateChecked")
    public Result updateChecked(@RequestParam(name = "skuId") String skuId,@RequestParam(name = "checked") Boolean checked){
        //获取令牌中的用户姓名
        String userName =
                TokenDecode.getUserInfo().get("username");

        cartService.updateChecked(skuId, checked, userName);
        return new Result(true, StatusCode.OK,"操作成功");
    }


}
