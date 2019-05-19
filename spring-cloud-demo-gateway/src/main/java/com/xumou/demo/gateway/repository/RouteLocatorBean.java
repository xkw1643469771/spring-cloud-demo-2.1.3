package com.xumou.demo.gateway.repository;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// 非动态路由配置
//@Configuration
public class RouteLocatorBean {

    @Bean
    RouteLocator routeLocator(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/**") // 拦截路径
                        .filters(f -> f.filter(filter())) // 过滤器
                        .uri("lb://WEB-ONE/one")) // 服务id， lb:// 表示从服务中心获取
                .build();
    }

    @Bean
    GatewayFilter filter(){
        return new GatewayFilter() {
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
                String token = queryParams.getFirst("token");
                if(StringUtils.isEmpty(token)){
                    ServerHttpResponse response = exchange.getResponse();
                    byte[] bytes = "token not is null".getBytes();
                    DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
                    return response.writeWith(Flux.just(dataBuffer));
                }
                return chain.filter(exchange);
            }
        };
    }

}
