package com.reactive.banker.controller;

import com.reactive.banker.entity.BankingAgg;
import com.reactive.banker.service.SimpleAggService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableMongoRepositories
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class SimpleAggServicePlusControllerTest {

    @Autowired
    private SimpleAggService aggService;

    @BeforeAll
    void testBefore() {

    }

    @Test
    @Order(value = 1)
    public void testService() {

        String aggId = "agg10101";
        //Flux<BankingAgg> bankingAggFlux = aggService.getAggregationsByAggId(aggId);
        Mono<Boolean> aggExist = aggService.existByAggregateId(aggId)
                .flatMap(x -> {
                    if (x) {
                        aggService.deleteAllByAggId(aggId);
                    } else {
                        BankingAgg agg = new BankingAgg();
                        agg.setAggregateId(aggId + "test");
                        agg.setAddNote("test Banking Agg");
                        aggService.saveBankingApp(agg);
                    }
                    return Mono.just(x);
                });
        aggExist
                .map(m -> assertThat(m).isTrue());
        StepVerifier.create(aggExist)
                .assertNext(c -> {
                    System.out.println("CC::" + c);
                })
                .verifyComplete();


    }

    @Test
    @Order(value = 2)
    public void testWithTuplesFunction() {

        String aggId = "agg1010";
        Flux<BankingAgg> bankingAggFlux = aggService.getAggregationsByAggId(aggId);
        Mono<Boolean> aggExist = aggService.existByAggregateId(aggId);

        Flux<BankingAgg> newFlux = Flux.zip(bankingAggFlux, Mono.just(aggId), aggExist)
                .flatMap(TupleUtils.function((flux1, aggId1, aggExist1) ->
                                deleteAggMaybe(flux1, aggId1, aggExist1)
                        )
                );

        StepVerifier.create(newFlux)
                //.expectNextCount(1)
                .verifyComplete();

    }

    @Test
    @Order(value = 5)
    public void testGetAggregateWithSingleContractPlusLists(){
        aggService.getAggregateWithSingleContractPlusLists("agg-1202", "ct-104",
                new String[]{"cl-101","cl-104"},new String[] {"p101","p102"},new String[]{"bk-102"})
                .subscribe(p -> {
                    log.debug("STRING {}", p.toString());
                });
    }

    private Flux<BankingAgg> deleteAggMaybe(BankingAgg fromFlux, String aggId, Boolean aggExist1) {

        log.debug("IN DOSOMETHING>>>");
        if (aggExist1) {
            aggService.deleteById(fromFlux.getId());
            //aggService.deleteAllByAggId(aggId);
            log.debug("deleting all...");
        }else{
            LocalDateTime date = LocalDateTime.now();
            fromFlux = new BankingAgg();
            fromFlux.setAggregateId(aggId);
            fromFlux.setAddNote(aggId + " with single contract and client, property, banker lists " + date.toString());
            aggService.saveBankingApp(fromFlux);
            log.debug("saving new one...");
        }
        log.debug("return just {} ", fromFlux.getId());
        return Flux.just(fromFlux);
    }
}
