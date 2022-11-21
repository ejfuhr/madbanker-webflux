package com.reactive.banker;

import com.reactive.banker.config.FullDataUtilities;
import com.reactive.banker.entity.*;
import com.reactive.banker.repository.*;
import com.reactive.banker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableMongoRepositories
@Slf4j
public class AggregateServiceTest {
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
    private SimpleAggRepository simpleAggRepository;
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
    @Autowired
    private FullDataUtilities utilities;

    /**
     * THIS now works: by saving/ipdating all with the flatmap - it works!!
     */
    @Test
    @Order(value = 1)
    //@Disabled
    public void testOneOnAggregation() {

        simpleAggRepository.delateAllWithAggregateId("agg-test101")
                .subscribe(s -> {
                    log.debug("deleting ;; {} {} ", s.getId(), s.getAggregateId());
                });

        Property p1 = new Property(null, "agg-p101", "smith2 st", "abq", "nm", "10101");
        Property p2 = new Property(null, "agg-p102", "snorkle mall", "abq", "nm", "10101");
        List<Property> propertyList = new ArrayList<Property>();
        propertyList.add(p1);
        propertyList.add(p2);
        Flux<Property> propertyFlux = propRep.saveAll(propertyList);
        Mono<List<Property>> propertyMonoList = propertyFlux.collectList();

        Client cl = new Client("agg-clientId");
        cl.setBusinessAddress("101 main");
        cl.setCity("bumkenville");
        cl.setFirstName("bobby tork");
        cl.setLastName("villa");
        cl.setMailCode("10101aa");
        cl.setState("MN");
        Mono<Client> monoClient = clientRep.save(cl);

        Contract ct1 = new Contract("agg-ct-101", 10100.23, 50000.01, 10.33);
        Contract ct2 = new Contract("agg-ct-102", 10600.23, 55000.01, 10.33);
        List<Contract> contractList = new ArrayList<Contract>();
        contractList.add(ct1);
        contractList.add(ct2);

        //Flux<Contract> fluxContract = contractRep.saveAll(contractList);
        Mono<List<Contract>> monoContractList = contractRep.saveAll(contractList).collectList();

        Mono<String> aggId = Mono.just("agg-test101");

        Mono<MadBanker> bk102 = bankerRep.findByBankerId("bk-102");

        Mono<BankingAgg> simpleAgg1 = utilities.postAggregateWithSingleClientBanker(
                aggId, monoClient, monoContractList, propertyMonoList,
                bk102
        );

        StepVerifier
                .create(simpleAgg1)
                .assertNext(no1 -> {
                    assertThat(no1.getAggregateId().equals("agg-test101"));
                    //assertNotNull(no1.getMadBanker(), "banker is null");
                })
                .expectComplete()
                .verify();
        //.verifyComplete();

        aggRepository.deleteAll(simpleAgg1)
                        .subscribe();

        aggRepository.count().subscribe(c -> {
            log.debug("The AGG COUNT >>>> {} ", c.longValue());
        });

/*        aggRepository.save(simpleAgg1.block())
                .map(agg -> {
                    assertThat(agg.getAggregateId() != null);
                    assertThat(agg.getAggregateId()).isEqualTo("agg-test101");
                    return agg;
                })
                .subscribe(s -> assertThat(s.getClientList().size() > 0));*/


    }

    private Mono<BankingAgg> buildInitialAggregate(Mono<String> aggId, Mono<Client> clientMono,
                                                   Mono<List<Contract>> contractMonnoList,
                                                   Mono<List<Property>> propertyMonoList,
                                                   Mono<MadBanker> bankerMono) {
        return Mono.zip(aggId, clientMono, contractMonnoList, propertyMonoList, bankerMono)
                .map(this::combine);

    }

    private BankingAgg combine(Tuple5<String, Client, List<Contract>, List<Property>, MadBanker> tuple) {

        return new BankingAgg(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3(),
                tuple.getT4(),
                tuple.getT5()
        );

    }

    @Test
    @Order(value = 2)
    //@Disabled
    public void testAggAnnotation() {
        Flux<Contract> contractFlux = aggRepository.findPropertiesByContractId("agg-ct-101");
        contractFlux
                .doOnNext(c -> {
                    assertNotNull(c);
                    log.debug("in verifier {}", c.toString());
                });
        contractFlux
                .subscribe(c3 -> {
                    System.out.println("XXXXXXXXXXXXX " + c3.getContractId());
                });


    }

    @Order(value = 3)
    @Test
    public void testGroupContractRaw(){

        Flux<BankingAgg> aggregationResultsFlux = aggRepository.groupContractRaw();
        aggregationResultsFlux
                .map(g -> {
                    System.out.println("::::::::::: "+ g.getClientList().size());
                            return g;

                        }
                )
                .doOnNext(c1 -> System.out.println("c1$$" + c1.toString()))
                .subscribe();
    }

    @Test
    @Order(value = 4)
    public void testUnwindOne(){
        Flux<Contract> unwind = aggRepository.unwindTrialOne();
        unwind
                .map(mp -> {
                    assertNotNull("null value", mp.getId());
                    assertNotNull("contractId null", mp.getContractId());
                    //assertThat(mp.getContractId()).doesNotContainAnyWhitespaces();
                    System.out.println("MP::::: " + mp.toString());
                    return mp;
                })
                .subscribe();
    }

    @Test
    @Order(value = 5)
    //@Disabled
    public void postAggregateWithSingleClientTest(){

/*        Mono<List<BankingAgg>> special = simpleAggRepository.findBankingAggsByAggregateId("addId-special-999")
                .collectList()


        ;

        special.subscribe(this::deleteByAggId);*/

        Mono<Client> cl = clientService.findByClientId("agg-clientId");
        Flux<Contract> contractFlux = contractService.getAllContracts();
        Mono<List<Contract>> contractMono = contractFlux.collectList();
        Mono<List<Property>> propertyListMono = propertyService.getAllProperties().collectList();
        Mono<List<MadBanker>> bankerListMono = bankerService.getAllBankers().collectList();
        //simpleAggService.postAggregateWithSingleClientBanker()

        Mono<String> addId = Mono.just("addId-special-999");

        Mono<BankingAgg> agg = simpleAggService.postAggregateWithSingleClient(addId, cl, contractMono,
                propertyListMono,
                bankerListMono);

        StepVerifier.create(agg)
                .assertNext(c -> assertThat(c.getBankerList().size()).isGreaterThan(1))
                .verifyComplete()
                ;

        cl.subscribe();
        contractMono.subscribe();
        propertyListMono.subscribe();
        bankerListMono.subscribe();
        addId.subscribe();

//        aggRepository.deleteAll(agg)
//                .subscribe();

    }

    private Mono<List<BankingAgg>> deleteByAggId(List<BankingAgg> list) {
        log.debug("previous to while....");
        while (list.listIterator().hasNext()) {
            BankingAgg agg = list.listIterator().next();
            simpleAggRepository.delete(agg);

        }
        Mono<List<BankingAgg>> x =  Mono.just(list);
        return x;
    }
}
