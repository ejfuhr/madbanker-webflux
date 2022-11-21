package com.reactive.banker.controller;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.service.ClientService;
import com.reactive.banker.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
public class ClientHandler {

    private final ClientService clientService;

    public  ClientHandler(@Autowired ClientService clientService){
        super();
        this.clientService = clientService;
    }

    @ResponseBody
    Mono<ServerResponse> allClients(ServerRequest r){

        log.debug("HERE IS R {} {} ::", r.path(), r.attributes().toString());
        Flux<Client> clientFlux = clientService.getAllClients();
        return ServerResponse
                .ok()

                .contentType(MediaType.APPLICATION_JSON)
                .body(clientFlux, Client.class)
                ;
    }

    Mono<ServerResponse> findByClientId(ServerRequest request) {
        Mono<Client> clientMono = request.bodyToMono(Client.class)
                        .flatMap(read -> clientService.findByClientId(request.pathVariable("clientId")));
        log.debug("request path and methodName {} {} ", request.path(), request.methodName());

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(clientMono, Client.class)
                .log()
                ;


    }

    public Mono<ServerResponse> createClient(ServerRequest request){
        return request
                .bodyToMono(Client.class)
                .flatMap(cl -> this.clientService.createClientAsObject(cl))
                .flatMap(p -> ServerResponse.created(URI.create("/clients/client/" + p.getId()))
                        .build());
    }


/*    Mono<ServerResponse> allClientsAgain(ServerRequest request){

        Flux<Client> clientFlux = clientService.getAllClients();
        request
                .body(clientFlux)
                ;


        return ServerResponse
                .ok()

                .contentType(MediaType.APPLICATION_JSON)
                .body(clientFlux, Client.class)
                ;
    }*/

/*    private staticxfile.txt Mono<ServerResponse> defaultReadResponse(Publisher<Contract> contracts) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(contracts, Contract.class);


        */

/*    private staticxfile.txt Mono<ServerResponse> defaultWriteResponse(Publisher<Property> properties) {
        return Mono
                .from(properties)
                .flatMap(p -> ServerResponse
                        .created(URI.create("/properties/" + p.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .build()
                );
    }*/
}
