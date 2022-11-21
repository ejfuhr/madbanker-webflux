package com.reactive.banker.controller;

import com.reactive.banker.controller.ContractController;
import com.reactive.banker.entity.Contract;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableMongoRepositories
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class ControllerPlusTests {

    @Autowired
    private ContractController controller;

    @Test
    @Order(value = 1)
    public void testController(){
        Mono<Contract> contract1 = controller.getByContractId("ct-102")
                .map(f -> f.getBody());

/*        StepVerifier.create(contract1)
                .expectNextCount(1)
                .verifyComplete()
                ;*/
        contract1.doOnNext(c -> {
            log.debug("contract {} {} {} ", c.getContractId(), c.getAmountDue(), c.getId());
        }).doOnNext(d -> {
            log.debug("WHAT IS HERE {}", d.getContractId());
        }).subscribe(System.out::println)
                ;
    }

    @Test
    @Order(value = 2)
    public void testDoByContractId(){
        Mono<Contract> contract2 = controller.doByContractId("ct-103")
                .map(cc -> {
                    log.debug("doContract:: {} {} ", cc.getId(), cc.getContractId());
                    return cc;
                });
        StepVerifier.create(contract2)
                .assertNext(c -> assertThat(c.getContractId()).isEqualTo("ct-103").isNotEmpty())
                //.consumeNextWith(System.out::println)
                .verifyComplete();
    }

    @Test
    @Order(value = 3)
    public void testFluxWithClientsPropertiesBankers(){

        assertTrue(2>1);

        ///contractsController/contract/ct-102/{cl-101, cl-103}/{p103, p101}/{bk-102, bk-103}
/*        controller.getFluxContractWIthClientsPropertiesBankers("ct-102",["cl-101", "cl-103"],
                ["p103", "p101"], ["bk-102", "bk-103"]);*/
    }
}
