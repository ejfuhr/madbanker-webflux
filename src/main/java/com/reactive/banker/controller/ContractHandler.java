package com.reactive.banker.controller;

import com.reactive.banker.entity.Contract;
import com.reactive.banker.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
public class ContractHandler {

    private final ContractService contractService;

    public ContractHandler(@Autowired ContractService contractService) {
        super();
        this.contractService = contractService;
    }

    public Mono<ServerResponse> createContract(ServerRequest r) {
        Mono<Contract> contractMono = r.bodyToMono(Contract.class)
                .flatMap(write -> contractService.saveContract(write));
                //.flatMap(w -> ServerResponse.ok().body())
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(contractMono, Contract.class)

                ;
    }

    public Mono<ServerResponse> updateContract(ServerRequest req){
        return Mono.zip(
                (data) -> {
                    Contract c = (Contract) data[0];//old
                    Contract c2= (Contract) data[1];//new values
                    c.setContractId(c2.getContractId());
                    c.setAmountDue(c2.getAmountDue());
                    c.setInterestPaid(c2.getInterestPaid());
                    c.setOriginalAmount(c2.getOriginalAmount());
                    c.setClients(c2.getClients());
                    c.setProperties(c2.getProperties());
                    c.setBankers(c2.getBankers());
                    return c;//return newly updated Contract
                },
                this.contractService.findByContractId(req.pathVariable("contractId")),
                req.bodyToMono(Contract.class)
        )
                .cast(Contract.class)
                .flatMap(c -> this.contractService.saveContract(c))
                .flatMap(c -> ServerResponse.ok().build());
    }

    public Flux<Part> createContractWithParts(ServerRequest r) {
        return r.body(BodyExtractors.toParts());
    }

    public Mono<ServerResponse> allContracts(ServerRequest r) {

        return defaultReadResponse(this.contractService.getAllContracts());
    }

    public Mono<ServerResponse> findByContractId(ServerRequest r) {
        Mono<Contract> contract = r.bodyToMono(Contract.class)
                .flatMap(toRead -> contractService.findByContractId(contractId(r)));
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(contract, Contract.class);
    }

    public Mono<ServerResponse> findByContractIdStreaming(ServerRequest r) {
        Mono<Contract> contract = r.bodyToMono(Contract.class)
                .flatMap(toRead -> contractService.findByContractId(contractId(r)));
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(contract, Contract.class);
    }

    private static Mono<ServerResponse> defaultReadResponse(Publisher<Contract> contracts) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(contracts, Contract.class);
    }

    private static Mono<ServerResponse> defaultWriteResponse(Publisher<Contract> properties) {
        return Mono
                .from(properties)
                .flatMap(p -> ServerResponse
                        .created(URI.create("/contracts/" + p.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .build()
                );
    }

    private static String contractId(ServerRequest r) {
        return r.pathVariable("contractId");
    }
}
