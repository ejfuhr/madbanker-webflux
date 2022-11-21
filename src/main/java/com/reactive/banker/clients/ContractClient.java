package com.reactive.banker.clients;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.Property;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
@Slf4j
public class ContractClient {

    private String url = "http://localhost:8080";
    private final WebClient client = WebClient
            .builder()
            .baseUrl(url)
            .build();

    public WebClient getClient(){ return this.client;}

    public Flux<Contract> responseEntityFlux(String contractId){

        return this.client
                .get()
                .uri("/contractsController/", contractId)
                .retrieve()
                .bodyToFlux(Contract.class)
                .onErrorResume(th -> Flux.empty())
                ;
    }

    public Flux<Contract> getAllContractsFlux(){
        return this.client
                .get()
                .uri("/contracts/")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeToFlux(ex -> ex.bodyToFlux(Contract.class))



                .onErrorResume(ex -> Flux.just(new Contract()))
                ;
    }
    public Mono<Contract> getContractByContractId(String contractId){
        return this.client
                .get()
                .uri(b -> b.queryParam("/contracts/", contractId).build())
                .retrieve()
                .bodyToMono(Contract.class)
                .onErrorResume(ex -> {
                    log.debug("ERROR ib Mono {}", ex.toString());
                    return Mono.empty();
                });

    }

    //http://localhost:8080/contractsController/contract/ct-103/cl-102,cl-103,cl-104/p103, p101/bk-102, bk-103
    //@GetMapping(path="/contractsController/contract/{contractId}/{clientIds}/{propertyIds}/{bankerIds}")
    public Flux<Contract> getFluxContractWIthLists(String contractId,
                                                   String[] clientIds, String[] propertyIds, String[] bankerIds){

            return this.client
                    .get()
                    .uri(entry -> entry.queryParam("/contractsController/contract",
                            contractId, clientIds, propertyIds, bankerIds).build())
                    .retrieve()
                    .bodyToFlux(Contract.class)
                    .onErrorResume(ex ->{
                        log.debug("ERROR on getFlux client {}", ex.toString());
                        return Flux.empty();
                    });
    }
}
