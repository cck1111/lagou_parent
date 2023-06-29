package com.lagou.order.service;

import java.util.Map;

/**
 * @author cck
 * @date 2023/6/29 11:19
 */
public interface CartService {
    void add(Integer num, Long id, String userName);

    Map list(String userName);

    void delete(String skuId, String userName);

    void updateChecked(String skuId, Boolean checked, String userName);
}
