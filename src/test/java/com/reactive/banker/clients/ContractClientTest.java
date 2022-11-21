package com.reactive.banker.clients;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Slf4j
public class ContractClientTest {

    @Autowired
    private ContractClient contractClient;

    @Test
    public void testAllClients(){
        Flux<Contract> contracts = contractClient.getAllContractsFlux();

        contracts
                .subscribe(s -> {
                    assertThat(s.getContractId()).isNotNull();
                    log.debug("s here:: {} {} ", s.getClients().size(), s.getProperties().size());
                });

    }
    @Test
    //@Disabled
    public void testContractClient(){

        Mono<Contract> contract = contractClient.getContractByContractId("ct-101");
        assertNotNull(contract);
        contract
                .map(c -> {
                    assertThat(c.getAmountDue()).isEqualTo(50000.01);
                    log.debug("does contract have clients, properites {} {}", c.getClients().size(),
                            c.getProperties().size());
                    return c;
                })
                .subscribe(s ->{
                    log.debug("subscriber {}", s.getClients());
                });

        assertNotNull(contractClient.getClient());
    }

    @Test
    public void testEntityResponseOnContactId(){

        Flux<Contract> contracts = contractClient.responseEntityFlux("ct-101")
            .doOnNext(n -> assertThat(n.getClients()).isNotNull());

        contracts
                .subscribe(p -> {
            log.debug("subbed responsieentity {} {} {} ", p.getContractId(), p.getClients(), p.getAmountDue());
        });

 /*          StepVerifier.create(contracts)
                //.expectNextCount(1)
               .assertNext(c -> {
                    assertThat(c.getContractId()).isEqualTo("ct-102");
                    log.debug("c clients {}", c.getClients().size());
                })
                .expectNextCount(4)
                //.consumeNextWith(cx -> assertThat(cx.getClients()).isNotNull())
                .verifyComplete()

  */
        ;



    }

    //contractsController/contract/ct-103/cl-102,cl-103,cl-104/p103, p101/bk-102, bk-103
    @Test
    public void testControllerContractWithLists(){

        //String[] clients = new String[] {"cl-102,cl-103,cl-104"};
        Flux<Contract> fromListsFlux = contractClient.getFluxContractWIthLists("ct-103",
                new String[] {"cl-102,cl-103,cl-104"},
                new String[]{"p103,p101"},
                new String[]{"bk-102,bk-103"});


        fromListsFlux
                .log()
                        .subscribe(c-> {
                            log.debug("subbibg stuff {} {} ", c.getClients().size(), c.getProperties().size());
                        });

/*        fromListsFlux
                .doOnNext(c->{
                    assertThat(c.getContractId()).isEqualTo("ct-103");
                    assertThat(c.getBankers().size()).isGreaterThanOrEqualTo(1);
                    assertThat(c.getClients().size()).isGreaterThanOrEqualTo(1);
                    System.out.println("doOnNext c:: " + c.toString());
                })
                .subscribe(c-> {
                    System.out.println("c:: " + c.toString());
                });*/

/*        StepVerifier.create(fromListsFlux)
                .assertNext(ct -> {
                    assertThat(ct.getClients().size()).isEqualTo(3);
                    assertThat(ct.getProperties().size()).isEqualTo(2);
                    assertThat(ct.getBankers().size()).isEqualTo(2);
                    assertThat(ct).isNotNull();

                })
                .verifyComplete();*/
    }
}
