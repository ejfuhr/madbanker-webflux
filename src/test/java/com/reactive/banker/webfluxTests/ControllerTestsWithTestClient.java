package com.reactive.banker.webfluxTests;

import com.reactive.banker.controller.ContractController;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

//@WebFluxTest(controllers= {ContractController.class})
//@Import(ContractService.class)
@SpringBootTest
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ControllerTestsWithTestClient {

    @Autowired
    private WebClient client;

    @Autowired
    ContractController contractController;

    @Test
    @Order(value = 1)
    public void testContractControllerFirst(){

        Flux<Contract> allContracts = client.get()
                .uri("/contractsController")
                .retrieve()
                .bodyToFlux(Contract.class)
                .doOnNext(c -> {
                    log.debug("c:: {}", c);
                })
                ;
        StepVerifier.create(allContracts)
                .expectNextCount(7)
                .verifyComplete()
                ;
    }
}
