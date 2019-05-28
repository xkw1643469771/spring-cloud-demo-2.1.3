package com.xumou.demo.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.net.URI;

//@Component
public class OneFilterFactory extends AbstractGatewayFilterFactory {

    public GatewayFilter apply(Object config) {
        // 变更请求的url过滤器
        return new OrderedGatewayFilter((exchange, chain) -> {
            String newUri = exchange.getRequest().getURI().toString().replace("/one", "");
            ServerHttpRequest req = exchange.getRequest().mutate().uri(URI.create(newUri)).build();
            return chain.filter(exchange.mutate().request(req).build());
        }, Integer.MIN_VALUE);
    }

    @Override
    public String name() {
        return "filterName";
    }
}
