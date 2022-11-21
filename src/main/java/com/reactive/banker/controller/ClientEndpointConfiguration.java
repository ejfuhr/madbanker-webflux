package com.reactive.banker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
@Slf4j
public class ClientEndpointConfiguration {

    @Bean
    RouterFunction<ServerResponse> clientRoutes(@Autowired ClientHandler handler) {

        log.debug("in returning routes... ClientEndpointConfiguration");
        return route(GET("/clients"), handler::allClients)
                .andRoute(GET("/clients/{clientId}"), handler::findByClientId)
                .andRoute(POST("/clients/client"), handler::createClient)
                ;
    }
}
