package com.reactive.banker;

import com.reactive.banker.entity.BankingAgg;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.service.SimpleAggService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableMongoRepositories
@Slf4j
public class SimpleAggTest {

    @Autowired
    private SimpleAggService simpleAggService;

    @Order(value = 1)
    @Test
    void testStringEntries() {
        Mono<BankingAgg> agg1 = simpleAggService.aggregate("agg-spec-1", "cl-101",
                "ct-102", "bk-102", "p104");
        agg1
               .flatMap(ba -> simpleAggService.saveBankingApp(ba))
                ;
        StepVerifier.create(agg1)
                .expectNextCount(1)
                .verifyComplete();
                //.subscribe();

    }

    @Order(value = 2)
    @Test
    void testFindFluxByAggId(){

        List<MadBanker> bList = new ArrayList<MadBanker>();
        Flux<BankingAgg> fx = simpleAggService.getAggregationsByAggId("addId-special-999");
        fx.subscribe(agg -> {
            log.debug("INSIDE ");
            bList.addAll(agg.getBankerList());
            //assertThat(bList.size()).isGreaterThan(2);
            printClientEtc(agg.getBankerList());
            assertThat(agg.getBankerList().size()).isGreaterThan(2);
            return ;
        });

        log.debug("blist {} ", bList.size());

        StepVerifier.create(fx)
                .expectNextCount(1)
                .verifyComplete();
        log.debug("blist {} ", bList.size());
    }

    private void printClientEtc(List<MadBanker> bankers){

        log.debug("BANKERS SIZE::: {}", bankers.size());
        bankers.stream().forEach(
                x -> {
                    log.debug("banker :: {} {} {} ", x.getBankerId(), x.getId(), x.getContracts().size());
                    //return x;
                }
        );
    }
}
