package com.reactive.banker.deconstruct;

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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Thi clas follows https://tolkiana.com/avoiding-nested-streams-spring-webflux/ by adding several functions on the
 * Tuples passed in the getContract() or getClient() or getBanker() operations.
 */

/*
see https://gist.github.com/pgilad/bac8a59e449dc470f3fc5954dd23761a
@WithMockUser
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = AggregateController.class)
@Import(CollectionService.class)
 */

@SpringBootTest
//@WebFluxTest
//@ExtendWith(SpringExtension.class)
//@WebFluxTest(excludeAutoConfiguration = FullDataSetupService.class)
@EnableMongoRepositories
@ActiveProfiles("classic")
@Slf4j
public class DeconstructByContractIdTest {

    @Autowired
    ReactiveMongoTemplate template;
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
    private ContractService contractService;
    @Autowired
    private BankerService bankerService;

    /**
     * from the article "First, we are going to use extensions to add functionality to the Tuple classes,
     * by introducing a function to flatten a Tuple2, that is to convert a Tuple2 object that contains
     * another Tuple2 into a Tuple3 that contains 3 simple objects..."
     *
     * and from
     * https://www.javacodegeeks.com/2020/01/project-reactor-de-structuring-a-tuple.html
     *
     */

    /*
        public PropertyContractState(Contract contract, List<Property> properties) {
        this.contract = contract;
        this.properties = properties;

        Tuple3<String, Integer, Integer> base = Tuples.of("Foo-York", 200, 300);
        Tuple2<?, ?> mapped = base.mapT1(String::length);
    }

  fun getProductById(productId: Int): Mono<Product> {
    return productRepository.findById(productId)
        .zipWith(colorRepository.findByProduct(productId).collectList())
        .zipWith(sizeRepository.findByProduct(productId).collectList())
        .map { it.flatten() }
        .map { (product, colors, sizes) -> product.copy(colors = colors, sizes = sizes) }
}

     */
    public Tuple3<Contract, Client, Property> flatten(Tuple2<Tuple2<Contract, Client>, Property> tx1) {
        //Tuple3<S, D, U>
        return Tuples.of(tx1.getT1().getT1(), tx1.getT1().getT2(), tx1.getT2());

    }

    public Tuple3<Contract, Client, Property> copy(Tuple3<Contract, Client, Property> t3) {
        t3.getT1().getClients().add(t3.getT2());
        t3.getT2().getContracts().add(t3.getT1());
        t3.getT1().getProperties().add(t3.getT3());

        return t3;
    }

    public Mono<Contract> getContractById(String contractId, String clientId, String propertyId) {
        return contractRep.findByContractId(contractId)
                .zipWith(clientRep.findByClientId(clientId))
                .zipWith(propRep.findByPropertyId(propertyId))
                .map(it -> flatten(it))
                //.map(it -> flatten(it.getT1()))
                .map(ct -> {

                    return copy(ct).getT1();
                })
                ;
    }

    @Test
    //@Disabled
    public void testAddClientPropertyToContract() {
        //ct-103, cl-101, p101

        removeContracts(5, 23)
                .subscribe(s -> {
                    log.debug("whats here " + s);
                });

        Mono<Contract> ct2 = getContractById("ct-103", "cl-101", "p101");
        ct2.map(c -> {
                    c.getProperties().get(0).setCity("taipai");
                    c.setAmountDue(202.22);
                    assertThat(c.getClients().get(0)).isInstanceOf(Client.class);
                    assertThat(c.getProperties().get(0)).isInstanceOf(Property.class);
                    assertNotNull(c, "c is null why??");
                    //;

                    return c;
                })
                .flatMap(d -> contractService.updateContractByContractId("ct-103", d));

        //ct2.subscribe();

        StepVerifier.create(ct2)
                //.assertNext(x -> assertNotNull(x, "x is null"))
                .assertNext(x -> assertThat(x.getAmountDue()).isBetween(202.22, 50000.01))
                //.expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void deconstructByjavaAllAndSundry() {

        Flux<String> stringFlux = Mono.zip(Mono.just("a"), Mono.just(3))
                .flatMapMany(TupleUtils.function((s, count) ->
                        Flux.range(1, count).map(i -> s + i)));

        stringFlux.subscribe(s -> {
            log.debug("subbed on a string {} ", s);
        });

    }

    @Test
    public void testContractClientPropertyWithFlatMap() {
        //ct-103, cl-101, p101
        //contractService.findByContractId("ct-103");
/*        Flux<Contract> contractFlux = Mono.zip(contractService.findByContractId("ct-101"),
                        clientService.findByClientId("cl-102"),
                        propertyService.findPropertyByPropertyId("p102")
                )
                .flatMapMany(TupleUtils.function((contrx, clientx, propx) ->
                        Flux.just(contrx, clientx, propx)
                                .flatMap(i -> doContract(contrx, clientx, propx))
                        ));*/

        Flux<Contract> contractMono = Mono.zip(contractService.findByContractId("ct-101"),
                        clientService.findByClientId("cl-102"),
                        propertyService.findPropertyByPropertyId("p102")
                )
                .flatMapMany(TupleUtils.function((contrx, clientx, propx) ->
                                //Flux.just(contrx, clientx, propx)
                                doContract(contrx, clientx, propx)
                        //.flatMap(n -> doContract(contrx, clientx, propx))
                ));

        contractMono
                .doOnNext(c -> {
                    assertThat(c.getContractId()).isEqualTo("ct-101");
                }).doOnNext(d -> {
                    assertThat(d.getClients().size()).isGreaterThanOrEqualTo(1);
                    assertThat(d.getProperties().size()).isGreaterThanOrEqualTo(1);
                }).subscribe(s -> {
                            log.debug("contract to string...{} ", s.toString());
                        }
                );

/*        StepVerifier.create(contractFlux)
                .assertNext(s -> {
                    assertNotNull(s, "S IS NULL??");
                })
                .expectNextCount(2)
                .expectComplete();*/
        //contractFlux.subscribe(s -> assertNotNull(s, "OBJECT IS NULL??"));
    }

    @Test
    public void testContractListsClientPropertyBankerWithFlatMap() {
        //get 2 clients in List
        //merge or mergewith
        //get 3 properties in List
        //add 1 MadBanker to List
        //Flux like in above
        //THEN check for nullness

        //starting with contract ct- or ct-105
        Mono<Contract> contractMono = contractService.findByContractId("ct-105");
        //add clients 103 and 104
        Mono<List<Client>> clientListMono = clientService.findByClientId("cl-103")
                .mergeWith(clientService.findByClientId("cl-104"))
                .collectList();
        //add pro[erties 104 105
        Mono<List<Property>> propertyListMono = propertyService.findPropertyByPropertyId("p104")
                .mergeWith(propertyService.findPropertyByPropertyId("p105"))
                .collectList();
        //add bankers bk-101 bk-102, bk-103

        Mono<List<MadBanker>> bankerListMono = bankerService.findBankerByBankerId("bk-101")
                .mergeWith(bankerService.findBankerByBankerId("bk-102"))
                .mergeWith(bankerService.findBankerByBankerId("bk-103"))
                .collectList();

        Flux<Contract> contractFlux = Mono.zip(contractMono, clientListMono, propertyListMono, bankerListMono)
                        .flatMapMany(TupleUtils.function((contrx, clientx, propx, bankerx) ->
                                        doContractWithClientPropertyBankerLists(contrx, clientx, propx, bankerx)
                        ));
                            //    .switchIfEmpty()

        contractFlux
                .doOnNext(c -> assertThat(c.getId()).isNotNull())
                .subscribe(c -> {
                    log.debug("am i here??");
                });

/*        StepVerifier.create(contractFlux)
                .assertNext(c -> assertThat(c.getId()).isNotNull())
                .assertNext(cl -> assertThat(cl.getClients().size()).isEqualTo(2))
                .expectNextCount(1)
                .verifyComplete();*/

    }

    private Mono<Contract> doContract(Contract contract, Client client, Property property) {
        contract.getClients().add(client);
        contract.getProperties().add(property);
        return contractService.saveContract(contract);//.flux();
        //return  Flux.just(new Contract());
    }

    private Mono<Contract> doContractWithClientPropertyBankerLists(Contract contract, List<Client> clientList,
                                                                   List<Property> propertyList,
                                                                   List<MadBanker> madBankerList) {
        contract.getClients().addAll(clientList);
        contract.getProperties().addAll(propertyList);
        contract.getBankers().addAll(madBankerList);
        return contractService.saveContract(contract);

    }

    private Mono<Void> removeContracts(int firstDocNo, int lastDockNo) {
        Flux<Contract> contractFlux = contractRep.findAll();
        return contractFlux.collectList()
                .flatMap(f -> {
                    if (f.size() > 10) {
                        List<Contract> bankList = f.subList(firstDocNo, lastDockNo);
                        return contractRep.deleteAll(bankList);
                    }
                    return Mono.empty();
                })
                ;
        //.subscribe();
    }

    private Mono<Tuple2<Client, Client>> giime2Contract() {

        return clientService.findByClientId("cl-102")
                .zipWith(clientService.findByClientId("cl-103"))
        //.flatMap(x -> x.)
        ;
    }



}

