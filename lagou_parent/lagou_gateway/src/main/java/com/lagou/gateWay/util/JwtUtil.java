package com.lagou.gateWay.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * @author cck
 * @date 2023/6/2 15:08
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
     * 解析
     *
     * @param token
     * @return
     * @throws Exception
     */
    public static Claims parseJWT(String token) throws Exception{
        SecretKey secretKey = generalKey();
        return Jwts.parser().setSigningKey(secretKey)
                .parseClaimsJws(token).getBody();
    }

}
