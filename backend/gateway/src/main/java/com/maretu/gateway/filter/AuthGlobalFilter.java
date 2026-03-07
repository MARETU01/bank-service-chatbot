package com.maretu.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maretu.common.dto.Context;
import com.maretu.common.utils.JwtUtils;
import com.maretu.gateway.config.AuthProperties;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final ObjectMapper jacksonObjectMapper;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        if (isExcludedPath(path)) {
            String token = exchange.getRequest().getHeaders().getFirst("Maretu");
            if (token == null || token.isEmpty()) {
                return chain.filter(exchange);
            }
            Context context;
            try {
                context = JwtUtils.parseJwt(token);
            } catch (ExpiredJwtException e) {
                // 构建错误响应
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                // 创建错误消息体
                String errorMessage = "{\"code\": 401, \"message\": \"JWT token expired\"}";
                byte[] bytes = errorMessage.getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = response.bufferFactory().wrap(bytes);

                return response.writeWith(Mono.just(buffer));
            }
            String userJson = jacksonObjectMapper.writeValueAsString(context);
            ServerWebExchange newExchange = exchange.mutate()
                    .request(builder -> builder.header("user-info", userJson))
                    .build();
            return chain.filter(newExchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst("Maretu");
        if (token == null || token.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        Context context = JwtUtils.parseJwt(token);
        String userJson = jacksonObjectMapper.writeValueAsString(context);
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header("user-info", userJson))
                .build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean isExcludedPath(String requestPath) {
        return authProperties.getExcludePaths().stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestPath));
    }
}
