package com.reactive.banker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
@Slf4j
public class BankerEndpointConfiguration {

    @Bean
    public RouterFunction<ServerResponse> bankerRoutes(@Autowired BankerHandler handler) {

        log.debug("in returning routes... BankerEndpointConfiguration");
        return route(GET("/banks"), handler::getAllBankers)

                .andRoute(GET("/banks/{bankerId}"), handler::getBankerByBankerId)
                .andRoute(PUT("/banks/{bankerId}/lastName/{lastName}/nickName/{nickName}/zip/{mailCode}/contracts/{contracts}"),
                        handler::updateBankerByBankerId)
                .andRoute(PUT("/banks/{bankerId}"), handler::updateByBankerBody)
                .andRoute(PUT("/banks/queryBanks"), handler::updateBankerWithQueryParams)
                .andRoute(POST("/banks/"), handler::createBanker)
                .andRoute(DELETE("/banks/deleteBanker/{id}"), handler::deleteBankerById)
                .andRoute(DELETE("/banks/deleteBanker/{bankerId}"), handler::deleteBankerByBankerId)

                ;
    }

    private static RequestPredicate b(RequestPredicate target) {
        return new BankerRequestPredicate(target);
    }
}


/*
            .andRoute(i(POST("/profiles")), handler::create)
            .andRoute(i(PUT("/profiles/{id}")), handler::updateById);
    }

    // <4>
    private staticxfile.txt RequestPredicate i(RequestPredicate target) {
        return new CaseInsensitiveRequestPredicate(target);
    }
 */