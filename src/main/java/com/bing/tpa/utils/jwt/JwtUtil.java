package com.bing.tpa.utils.jwt;

import cn.hutool.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * jwt工具类
 */
public class JwtUtil {

    //有效期
    public static final Long JWT_TTL=60*60*1000L;

//    秘钥明文
    public static final String JWT_KEY="liuchong";

    public static String getUUID(){
        String token= UUID.randomUUID().toString().replace("-","");
        return token;
    }

    /**
     * 生成jwt
     */
    public static String createJWT(String subject,Long ttlMillis){
        JwtBuilder builder=getJwtBuilder(subject,ttlMillis,getUUID());
        return builder.compact();
    }

    /**
     * 生成jwt
     */
    public static String createJWT(String subject){
        JwtBuilder jwtBuilder = getJwtBuilder(subject, null, getUUID());
        return jwtBuilder.compact();
    }

    private static JwtBuilder getJwtBuilder(String subject,Long ttlMillis,String uuid){
        SignatureAlgorithm signatureAlgorithm=SignatureAlgorithm.HS256;
        SecretKey secretKey=generalKey();
        long nowMillis=System.currentTimeMillis();
        Date now= new Date(nowMillis);
        if(ttlMillis==null){
            ttlMillis= JwtUtil.JWT_TTL;
        }
        long expMillis=nowMillis+ttlMillis;
        Date expDate=new Date(expMillis);
        return Jwts.builder()
                .setId(uuid)
                .setSubject(subject)
                .setIssuer("lc")
                .setIssuedAt(now)
                .signWith(signatureAlgorithm,secretKey)
                .setExpiration(expDate);
    }

    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }

    /**
     * 创建token
     */
    public static String createJWT(String id,String subject,Long ttlMillis){
        JwtBuilder builder = getJwtBuilder(subject, ttlMillis, id);
        return builder.compact();
    }
    public static void main(String [] args){
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJjYWM2ZDVhZi1mNjVlLTQ0MDAtYjcxMi0zYWEwOGIyOTIwYjQiLCJzdWIiOiJzZyIsImlzcyI6InNnIiwiaWF0IjoxNjM4MTA2NzEyLCJleHAiOjE2MzgxMTAzMTJ9.JVsSbkP94wuczb4QryQbAke3ysBDIL5ou8fWsbt_ebg";
        Claims claims = parseJWT(token);
        System.out.println(claims);
    }

    /**
     * 解析jwt
     */
    public static Claims parseJWT(String jwt){
        SecretKey secretKey=generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }

}
