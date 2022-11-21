package com.reactive.banker.controller;

import com.reactive.banker.entity.*;
import com.reactive.banker.service.SimpleAggService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "/agg", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class AggregateController {

    private final MediaType mediaType = MediaType.APPLICATION_JSON;
    @Autowired
    SimpleAggService simpleAggService;

    @GetMapping(path = "/findAllAggregations")
    @ResponseBody
    public Flux<BankingAgg> findAllAggregations() {
        return this.simpleAggService.findAllAggregations()
        ;
    }

            /*
                Flux<BankingAgg> bankingAggFlux = Mono.zip(bankingAggMono, contractMono, clientsListMono, propertiesListMono, bankersListMono)
                .flatMapMany(TupleUtils.function((aggx, contrx, clientx, propx, bankerx) ->
                        doAggregateWithContractPlusLists(aggx, contrx, clientx, propx, bankerx)
                ));
         */

    @DeleteMapping(path = "/deleteAllByAggId/{aggId}")
    @ResponseBody
    public Flux<BankingAgg> deleteAllByAggId(@PathVariable String aggId) {
        return this.simpleAggService.deleteAllByAggId(aggId);
    }

    @DeleteMapping(path = "/deleteByAggId/{aggId}")
    @ResponseBody
    public Mono<BankingAgg> deleteByAggId(@PathVariable String aggId) {
        return this.simpleAggService.deleteByAggregateId(aggId);
    }

    @PostMapping(path = "/createSimpleAgg")
    @ResponseBody
    public Mono<ResponseEntity<BankingAgg>> createSimpleBankingAgg(
            @RequestBody BankingAgg bankingAgg) {


        LocalDateTime date = LocalDateTime.now();
        String aggregateId = "defaultId" + date.toString();
        if (null == bankingAgg.getAddNote()) {
            bankingAgg.setAddNote(aggregateId + " with single contract and client, property, banker lists " + date.toString());
            bankingAgg.setAggregateId(aggregateId);
        }

        return this.simpleAggService
                .saveBankingApp(bankingAgg)
                .map(p -> ResponseEntity.created(URI.create("/simpleAggregate/" + p.getAggregateId()))
                        .contentType(mediaType)
                        .build()
                )
                ;


    }


    /**
     * try this with "/agg/aggregate/agg-101/ct-102/cl-101, cl-103/p103, p101/bk-102, bk-103
     *
     * @param aggId
     * @param contractId
     * @param clientIds
     * @param propertyIds
     * @param bankerIds
     * @return
     */
    @GetMapping(path = "/aggregate/{aggId}/{contractId}/{clientIds}/{propertyIds}/{bankerIds}")
    @ResponseBody
    public Flux<BankingAgg> getAggregateWithAggIdContractAndLists(@PathVariable String aggId,
                                                                  @PathVariable String contractId,
                                                                  @PathVariable String[] clientIds,
                                                                  @PathVariable String[] propertyIds,
                                                                  @PathVariable String[] bankerIds) {
        return this.simpleAggService.getAggregateWithSingleContractPlusLists(
                aggId, contractId, clientIds, propertyIds, bankerIds
        );
    }

    @PostMapping(path = "/newAggId/{aggId}/clientId/{clientId}/{contractIds}/{propertyIds}/{bankerIds}")
    @ResponseBody
    public Mono<ResponseEntity<List<BankingAgg>>> postNewAggregateWithClientPlusContractsPropertiesBankers(@PathVariable String aggId,
                                                                                                          @PathVariable String clientId,
                                                                                                          @PathVariable String[] contractIds,
                                                                                                          @PathVariable String[] propertyIds,
                                                                                                          @PathVariable String[] bankerIds ){
        return this.simpleAggService.postNewAggregateWithSingleClientPlusLists(
                aggId, clientId, contractIds, propertyIds, bankerIds
        ).collectList()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
/*                .map(p -> ResponseEntity.created(URI.create("/newAggId/" + p.size()))
                        .contentType(mediaType)
                        .build()
                )*/

    }

/*
get the entries as @Param string id's like in Contract
@ResponseBody
    public Mono<BankingAgg> postAggregateWithAggIdAndClientContractProperty
    Mono<BankingAgg> postAggregateWithSingleClient(Mono<String> aggId, Mono<Client> clientMono,
                                                   Mono<List<Contract>> contractMonoList,
                                                   Mono<List<Property>> propertyMonoList,
                                                   Mono<List<MadBanker>> bankerMonoList)*/

/*    public Flux<ResponseEntity<BankingAgg>>findAllAggregations(){
        return this.simpleAggService.findAllAggregations()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

 */
}
