package com.lagou.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * @author cck
 * @date 2023/6/29 16:08
 */
@Component
public class FeignInterceptor  implements RequestInterceptor {

    /**
     * Feign之前调用拦截
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();
//获得request中的请求头信息，放入到template中
        Enumeration<String> headerNames =
                requestAttributes.getRequest().getHeaderNames();
//遍历
        while(headerNames.hasMoreElements()){
            String headerKey = headerNames.nextElement();
//通过key获取value
            String headerValue =
                    requestAttributes.getRequest().getHeader(headerKey);
            template.header(headerKey,headerValue);
        }
    }
}
