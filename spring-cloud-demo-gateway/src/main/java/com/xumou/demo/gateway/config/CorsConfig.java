package com.xumou.demo.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class CorsConfig {

    private static final String MAX_AGE = "18000L";

    @Bean
    @Order(-1)
    public WebFilter corsFilter() {
        // 跨域设置
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            if (CorsUtils.isCorsRequest(request)) {
                HttpHeaders requestHeaders = request.getHeaders();
                ServerHttpResponse response = ctx.getResponse();
                HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();
                HttpHeaders headers = response.getHeaders();
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());
                headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders
                        .getAccessControlRequestHeaders());
                if(requestMethod != null){
                    headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());
                }
                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");
                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                if (request.getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }

            }
            return chain.filter(ctx);
        };
    }

    @Bean
    @Order(-2)
    public WebFilter webFilter(){
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            System.out.println(request.getURI());

            System.out.println(request.getCookies().get("token"));
            exchange.getResponse().addCookie(ResponseCookie
                    .from("token",Math.random()+"")
                    .path("/")
                    .build());


            System.out.println(request.getHeaders().get("token"));
            exchange.getResponse().getHeaders().add("token",Math.random() + "");

            return chain.filter(exchange);
        };
    }

}
