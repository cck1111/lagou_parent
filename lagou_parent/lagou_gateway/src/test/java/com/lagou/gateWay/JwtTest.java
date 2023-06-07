package com.lagou.gateWay;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

/**
 * @author cck
 * @date 2023/6/2 11:07
 */
public class JwtTest {

    @org.junit.Test
    public void testJwt1(){
        // 构建一个jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527").setSubject("lagou_shop").setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "lagou");
        // 创建
        String token = jwtBuilder.compact();
        System.out.println("token");

        // 解析 验证 token
        Claims lagou = Jwts.parser().setSigningKey("lagou").parseClaimsJws(token).getBody();
        System.out.println(lagou);
    }

    /**
     *  创建带有过期时间的token
     */
    @org.junit.Test
    public void testJwt2(){
        // 过期时间
        long expirTime = 1000L * 24;
        Date date = new Date(expirTime);

        // 构建一个jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527").setSubject("lagou_shop")
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS256, "lagou");
        // 创建
        String token = jwtBuilder.compact();
        System.out.println("token");

        // 解析 验证 token
        Claims lagou = Jwts.parser().setSigningKey("lagou").parseClaimsJws(token).getBody();
        System.out.println(lagou);
    }

    /**
     * 创建带有自定义载荷的token
     */
    @org.junit.Test
    public void testJwt3(){
        // 过期时间
        long expirTime = 1000L * 24;
        Date date = new Date(expirTime);

        // 构建一个jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527").setSubject("lagou_shop")
                .setIssuedAt(new Date())
                .setExpiration(date)
                .claim("role","admin")
                .signWith(SignatureAlgorithm.HS256, "lagou");
        // 创建
        String token = jwtBuilder.compact();
        System.out.println("token");

        // 解析 验证 token
        Claims lagou = Jwts.parser().setSigningKey("lagou").parseClaimsJws(token).getBody();
        System.out.println(lagou);
    }
}
