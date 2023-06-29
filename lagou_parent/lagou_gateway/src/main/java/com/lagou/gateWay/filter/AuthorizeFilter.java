package com.lagou.gateWay.filter;

import com.lagou.gateWay.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 鉴权过滤器
 * @author cck
 * @date 2023/6/2 15:11
 */
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //private static final String AUTHORIZE_TOKEN = "token";
    private static final String AUTHORIZE_TOKEN = "Authorization";

    /**
     *  请求时，是将token放入请求head中的
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        // 1.如果用户访问的是登录则放行 (后台 与 前台)
        if (path.contains("/admin/login") || path.contains("/user/login")){
            return chain.filter(exchange);
        }
        // 2.如果用户没有携带token,错误提示
        if (StringUtils.isEmpty(token)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 3.解析失败
//        try {
//            JwtUtil.parseJWT(token);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            return response.setComplete();
//        }
        // 4.合法访问
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
