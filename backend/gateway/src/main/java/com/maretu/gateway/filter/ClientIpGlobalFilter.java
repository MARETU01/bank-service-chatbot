package com.maretu.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

/**
 * 在网关层提取客户端真实 IP，并透传给下游服务。
 * <p>
 * 写入请求头：X-Client-IP
 * <p>
 * 注意：只有在网关前置代理/负载均衡是可信的情况下，才应该信任 X-Forwarded-For/Forwarded 等头。
 */
@Component
public class ClientIpGlobalFilter implements GlobalFilter, Ordered {

    public static final String CLIENT_IP_HEADER = "X-Client-IP";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = extractClientIp(exchange);
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> builder.header(CLIENT_IP_HEADER, clientIp))
                .build();
        return chain.filter(newExchange);
    }

    /**
     * 提取客户端真实 IP：
     * 1) X-Forwarded-For (取第一个)
     * 2) X-Real-IP / Proxy-Client-IP / WL-Proxy-Client-IP
     * 3) Forwarded: for=...
     * 4) remoteAddress
     */
    private String extractClientIp(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        String xff = headers.getFirst("X-Forwarded-For");
        String fromXff = firstIpFromCommaSeparated(xff);
        if (isValidIpToken(fromXff)) {
            return normalizeIpToken(fromXff);
        }

        for (String name : List.of("X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP")) {
            String v = headers.getFirst(name);
            if (isValidIpToken(v)) {
                return normalizeIpToken(v);
            }
        }

        String forwarded = headers.getFirst("Forwarded");
        String fromForwarded = firstForFromForwardedHeader(forwarded);
        if (isValidIpToken(fromForwarded)) {
            return normalizeIpToken(fromForwarded);
        }

        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return Optional.ofNullable(remote)
                .map(InetSocketAddress::getAddress)
                .map(Object::toString)
                .map(this::stripLeadingSlash)
                .orElse("unknown");
    }

    private String firstIpFromCommaSeparated(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return null;
        }
        int comma = v.indexOf(',');
        return (comma >= 0 ? v.substring(0, comma) : v).trim();
    }

    private String firstForFromForwardedHeader(String forwarded) {
        if (forwarded == null) {
            return null;
        }
        int idx = indexOfIgnoreCase(forwarded, "for=");
        if (idx < 0) {
            return null;
        }

        String tail = forwarded.substring(idx + 4).trim();
        int end = tail.length();
        int semi = tail.indexOf(';');
        if (semi >= 0) end = Math.min(end, semi);
        int comma = tail.indexOf(',');
        if (comma >= 0) end = Math.min(end, comma);
        String token = tail.substring(0, end).trim();

        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
            token = token.substring(1, token.length() - 1).trim();
        }

        if (token.startsWith("[") && token.contains("]")) {
            int rb = token.indexOf(']');
            token = token.substring(1, rb).trim();
        }

        // best-effort strip ipv4:port
        int colon = token.lastIndexOf(':');
        if (colon > 0 && token.indexOf(':') == colon) {
            String maybePort = token.substring(colon + 1);
            if (maybePort.chars().allMatch(Character::isDigit)) {
                token = token.substring(0, colon);
            }
        }

        return token.trim();
    }

    private int indexOfIgnoreCase(String source, String target) {
        return source.toLowerCase().indexOf(target.toLowerCase());
    }

    private boolean isValidIpToken(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        return !v.isEmpty() && !"unknown".equalsIgnoreCase(v);
    }

    private String normalizeIpToken(String value) {
        return stripLeadingSlash(value.trim());
    }

    private String stripLeadingSlash(String s) {
        return (s != null && s.startsWith("/")) ? s.substring(1) : s;
    }

    /**
     * 让该过滤器尽量靠前执行，方便后续过滤器/下游使用 X-Client-IP。
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

