package com.lagou.system.util;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * jwt 工具类
 * @author cck
 * @date 2023/6/2 11:38
 */
public class JwtUtil {

    /**
     * 有效期  60 * 60 * 1000 1小时
     * */
    public static final Long JWT_TTL = 360000L;

    /**
     *  密钥明文
     */
    public static final String JWT_KEY = "lagou";

    /**
     * 生成加密后的秘钥 secretKey
     * @return
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    /**
     *  创建token
     * @param id  令牌的id
     * @param subject  令牌主题
     * @param ttlMillis  有效期
     * @return
     */
    public static String creatJwt(String id, String subject, Long ttlMillis){
        if (ttlMillis == null){
            ttlMillis = JWT_TTL;
        }

        Date expirTime = new Date(System.currentTimeMillis() + ttlMillis);
        // 获取密钥密文
        SecretKey secretKey = generalKey();
        JwtBuilder builder = Jwts.builder()
                //唯一的ID
                .setId(id)
                // 主题 可以是JSON数 据
                .setSubject(subject)
                // 签发者
                .setIssuer("admin")
                // 签发时间
                .setIssuedAt(new Date())
                //使用HS256对称加密算法签名, 第二个参数为秘钥
                .signWith(SignatureAlgorithm.HS256, secretKey)
// 设置过期时间
                .setExpiration(expirTime);
        return builder.compact();
    }
}
