package com.reactive.banker.tupleTests;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.entity.Property;
import com.reactive.banker.repository.*;
import com.reactive.banker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple4;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableMongoRepositories
@Slf4j
public class TrialTest {

    @Autowired
    private ContractService contractService;
    @Autowired
    private PropertyRepository propRep;

    @Autowired
    private ClientRepository clientRep;

    @Autowired
    private ContractRepository contractRep;

    @Autowired
    private MadBankerRepository bankerRep;
    @Autowired
    private BankingAggRepository aggRepository;
    @Autowired
    private SimpleAggService simpleAggService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private BankerService bankerService;

    /**
     * Mono and Mom<List/> a bunch of enitiies (Contract, Clients, Properties, Bankers. Then flatMap and
     * function/ consume /predicate in diffrent tests
     */

    private Mono<Tuple4<Contract, List<Client>, List<Property>, List<MadBanker>>>
            getContractTupleWithContractListsOfClientsPropertiesBankers(String contractId, String[] clientIds,
                                                                        String[] propertyIds, String[] bankerIds) {
        Mono<Contract> contractMono = contractService.findByContractId(contractId);

        Mono<List<Client>> clientsListMono = clientService.findClientsWithClientIdsArray(clientIds);
        Mono<List<Property>> propertiesListMono = propertyService.findPropertiesWithPropertyIdsArray(propertyIds);
        Mono<List<MadBanker>> bankersListMono = bankerService.findBankersWithBankerIdsArray(bankerIds);

        return Mono.zip(contractMono, clientsListMono, propertiesListMono, bankersListMono);
    }


    @Test
    public void testContractFluxWithConsumerPredicate() {
/*        Flux<Contract> contractFlux = contractService.getContractFluxWithContractListsOfClientsPropertiesBankers(
                "ct-102",
                new String[]{"cl-101", "cl-102"}, new String[]{"p103", "p102"}, new String[]{"bk-102", "bk-103"});*/

        Flux<Contract> contractFlux1 = getContractTupleWithContractListsOfClientsPropertiesBankers("ct-102",
                new String[]{"cl-101", "cl-102"}, new String[]{"p103", "p102"}, new String[]{"bk-102", "bk-103"})
                .flatMapMany(TupleUtils.function((contrx, clintx, propx, bankx) ->
                        doFunction(contrx, clintx, propx, bankx)));

        StepVerifier.create(contractFlux1.collectList())
                .expectNextCount(1)

                .verifyComplete();
    }

    @Test
    public void testPredicate(){
        Flux<Contract> contractFlux1 =
                getContractTupleWithContractListsOfClientsPropertiesBankers("ct-102",
                new String[]{"cl-101", "cl-102"}, new String[]{"p103", "p102"}, new String[]{"bk-102", "bk-103"})
                        .flatMapMany(TupleUtils.function((ct, cl, pr, bk) -> {
                    assertThat(ct.getClass()).isInstanceOf(Contract.class);
                    //return ct.getClass().isInstance(ct);
                    return contractService.saveContract(ct);
                }));
    }

    @Test
    public void testConsumer(){
        Flux<Contract> contractFlux1 =
                getContractTupleWithContractListsOfClientsPropertiesBankers("ct-102",
                        new String[]{"cl-101", "cl-102"}, new String[]{"p103", "p102"}, new String[]{"bk-102", "bk-103"})
                        .flatMapMany(TupleUtils.function((ct, cl, pr, bk) -> {
                            assertThat(ct.getClass()).isInstanceOf(Contract.class);
                            //return ct.getClass().isInstance(ct);
                            return contractService.saveContract(ct);
                        }))
                        //.subscribe(c -> c.g)
        ;
    }


        private Mono<Contract> doFunction(Contract contract, List<Client> clientList,
                                          List<Property> propertyList, List<MadBanker> bankerList) {
            contract.getClients().addAll(clientList);
            contract.getProperties().addAll(propertyList);
            contract.getBankers().addAll(bankerList);
            return contractService.saveContract(contract);
        }


}


