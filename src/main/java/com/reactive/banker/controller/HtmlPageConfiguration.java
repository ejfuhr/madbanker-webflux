package com.reactive.banker.controller;

import lombok.extern.slf4j.Slf4j;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
@EnableWebFlux
@Slf4j
public class HtmlPageConfiguration {

    @Bean
    public RouterFunction<ServerResponse> HtmlRouter(
            @Value("classpath:/static/index.html") Resource html) {
        return route(GET("/"),
                request
                        -> ok().contentType(MediaType.TEXT_HTML)
                        .bodyValue(html)
                //.syncBody(html)
        );
    }

    @Bean
    public RouterFunction<ServerResponse> AllHtmlRouter() {
        return RouterFunctions
                .resources("/**", new ClassPathResource("static/"));
    }
}
