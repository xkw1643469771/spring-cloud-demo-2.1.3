package com.xumou.demo.gateway.repository;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class RouteDefinitionRepositoryImpl implements RouteDefinitionRepository {

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {

        RouteDefinition routeOne = new RouteDefinition();
        routeOne.setUri(URI.create("lb://WEB-ONE"));
        routeOne.getFilters().add(new FilterDefinition("filterName=arg1,arg2"));
        routeOne.getPredicates().add(new PredicateDefinition("Path=/one/*,/two/*"));

        return Flux.fromIterable(Arrays.asList(routeOne));
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return null;
    }

    public static void pring(String str){
        System.out.println(str);
    }

    public static void main(String[] args) {
        List<String> ss = Arrays.asList("a", "b", "c", "d");
        ss.forEach(RouteDefinitionRepositoryImpl::pring);
    }

}
