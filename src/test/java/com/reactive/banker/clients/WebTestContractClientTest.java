package com.reactive.banker.clients;

import com.reactive.banker.entity.Contract;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableMongoRepositories
//@WebFluxTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class WebTestContractClientTest {

    @Autowired
    private ApplicationContext context;
    private WebTestClient client;

    @BeforeAll
        void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }
    //@BeforeEach


    @Test
    void testClient() {


        String contractId = "ct-101";

        client
                .get()
                .uri("/contractsController/{contractId}", contractId)

                .exchange()
                .expectStatus().isOk()
                .expectBody(Contract.class)


                //.returnResult()

                .consumeWith(System.out::println)

 /*                 .consumeWith(cc -> assertThat(cc.getResponseBody().getContractId()).isEqualTo("ct-101"))
                .consumeWith(ccc -> {
                    log.debug("contract id {} ",ccc.getResponseBody().getContractId());
                })*/
                /*
                .expectBody()//.consumeWith(c -> log.debug(c.getResponseBodyContent().toString()))

                //.jsonPath("$.name").isNotEmpty()
                .jsonPath("$.contractId").isEqualTo(contractId)
                .jsonPath("$.amountDue").isEqualTo(50000.01)
                .jsonPath("$.clients").value(p -> {
                    System.out.println("P is :: " + p);
                })
                No matching handler in postman error
*/
        ;
    }

}
