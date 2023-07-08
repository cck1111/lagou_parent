package com.lagou.oauth.interceptor;

import com.lagou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

/**
 * @author 元敬
 * @Version 1.0
 */
@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        //创建管理员令牌
        //String token = AdminToken.create();
        //放入Feign请求头中
        template.header("Authorization","bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTY4ODgzNTI5MCwiYXV0aG9yaXRpZXMiOlsidmlwIiwiYWRtaW4iLCJ1c2VyIl0sImp0aSI6ImE3OWFkMjkzLWY2MDUtNDMwMC1iNWUzLTVjMWE1NTUyODQ3YyIsImNsaWVudF9pZCI6ImxhZ291IiwidXNlcm5hbWUiOiJsYWdvdSJ9.g6sxK_PBYBDzDf9HrXdaqfzIWc09wB9DYD-VlOR1mXgfL1m91QMEBLWdtvFXd0fDfCs8nWZzmsG9zy_43US1GCf7dWd_g_PLlVZ9NTWwC4glgQKVH7rZvY-gm2i5STDxvzN6fGkVsI-vcM-N1axJO0Omu0Mwp5iT1l-YZBBHrBC4aipV7grGOeDVfu6ImrpMS6Mhc8ZEzbO8USFkKx-MnObbkOxhK7YuvF7IUXWgUswbvHSBpm1KyVD5cWFb97AwVA4DpjQOtK8wjA7zUFgFoFubKxd6N1bBccsPmLq-LscCwRf25xqaqJlrMqd0TZ9nWcp-DscOoi2j3h26Rd6fHg");
    }
}
