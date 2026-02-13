package com.maretu.common.utils;

import com.maretu.common.dto.Context;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {

    private static final String SECRET = "maretumaretumaretumaretumaretuaa";
    private static final SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final Long expirationTime = 86400000L;

    private static Map<String, Object> generateClaim(Context context) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", context.getUsername());
        claims.put("userId", context.getUserId());
        claims.put("email", context.getEmail());
        return claims;
    }

    public static String generateJwt(Context context) {
        return Jwts.builder()
                .claims(generateClaim(context))
                .signWith(signingKey)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public static String logOutJwt(Context context) {
        return Jwts.builder()
                .claims(generateClaim(context))
                .signWith(signingKey)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public static Context parseJwt(String jwt) {
        Claims claims =  Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(jwt).getPayload();
        Context context = new Context();
        context.setUserId(Integer.parseInt(claims.get("userId").toString()));
        context.setUsername(claims.get("username", String.class));
        context.setEmail(claims.get("email", String.class));
        return context;
    }
}