package com.reactive.banker.clients;

import com.reactive.banker.MadbankerWebfluxApplication;
import com.reactive.banker.controller.ContractEndpointConfiguration;
import com.reactive.banker.controller.ContractHandler;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.service.ContractService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
@SpringBootTest
@EnableMongoRepositories
//@WebFluxTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {MadbankerWebfluxApplication.class, ContractEndpointConfiguration.class, ContractHandler.class})
@Import(ContractService.class)
public class WebTestContractRouterTest {

    private WebTestClient client;

    @Autowired
    private ApplicationContext context;
    @Autowired
    private ContractEndpointConfiguration config;

    @Autowired
    private ContractService contractService;

    @MockBean
    private ContractHandler handler;

//RouterFunction<ServerResponse> contractRoutes(@Autowired ContractHandler handler)
    @BeforeAll
    public void setClient() {
        //client = WebTestClient.bindToApplicationContext(context).build();
        client = WebTestClient.bindToRouterFunction(config.contractRoutes(handler)).build();

    }

    @Test
    @Disabled
    void testOne(){
        assertNotNull(client);

/*        Mockito.when(handler.allContracts(Mockito.any()))
                .thenReturn(ServerResponse.ok().bodyValue(contractService.getAllContracts()));*/

        this.client
                .get()
                .uri("/contracts")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Contract.class)
                .consumeWith(System.out::println)
                //.value(c -> assertNotNull(c, "c is NULL??"))
                ;
    }
}
