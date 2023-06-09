package com.lagou.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author cck
 * @date 2023/6/16 15:37
 */
@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {

    /***
     * 静态资源放行
     * @param registry
     */
    @Override
    public void
    addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/items/**")
                .addResourceLocations("classpath:/templates/items/");
    }

}
