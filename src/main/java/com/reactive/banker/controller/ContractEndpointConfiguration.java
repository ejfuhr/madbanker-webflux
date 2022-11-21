package com.reactive.banker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
@EnableWebFlux
@Slf4j
public class ContractEndpointConfiguration {

    @Bean
    public RouterFunction<ServerResponse> contractRoutes(@Autowired ContractHandler handler) {

        log.debug("in returning routes... ContractEndpointConfiguration");
        return route(GET("/contracts"), handler::allContracts)

                .andRoute(GET("/contracts/{contractId}"), handler::findByContractId)
                .andRoute(PUT("/contracts/{contractId}"), handler::updateContract)
                .andRoute(POST("/contracts/contract"), handler::createContract)

                ;
    }

}