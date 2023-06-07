package com.lagou.gateWay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author cck
 * @date 2023/6/1 13:39
 */
@SpringBootApplication
@EnableEurekaClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class,args);
    }

    /**
     *  定义一个KeyResolver
     *  通过ip进行限流
     */
    @Bean
    public KeyResolver ipKeyResolver(){
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName());
    }

    /**
     *  定义一个KeyResolver
     *  通过用户进行限流  根据用户唯一识别 userId
     */
    public KeyResolver userKeyResolver(){
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getQueryParams().getFirst("userId")));
    }

    /**
     *  定义一个KeyResolver
     *  通过请求路径进行限流
     */
    public KeyResolver urlKeyResolver(){
        return exchange -> Mono.just(Objects.requireNonNull(exchange.getRequest().getPath().value()));
    }
}

