package com.reactive.banker;

import com.reactive.banker.entity.BankingAgg;
import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.repository.BankingAggRepository;
import com.reactive.banker.repository.ContractRepository;
import com.reactive.banker.repository.PropertyRepository;
import com.reactive.banker.service.ClientService;
import com.reactive.banker.service.ContractService;
import com.reactive.banker.service.SimpleAggService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableMongoRepositories
@ActiveProfiles("classic")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class ContractPublisherPlusTupleToysTest {

    @Autowired
    ReactiveMongoTemplate template;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private PropertyRepository propRepository;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ContractRepository contractRep;
    @Autowired
    private ClientService clientService;
    @Autowired
    private BankingAggRepository aggRepository;
    @Autowired
    private SimpleAggService simpleAggService;


    private void reduceContracts(String contractId){
        Contract contract= new Contract();
        contract.setContractId(contractId);

        Example<Contract> example = Example.of(contract);
        Mono<List<Contract>> contractList = contractRep.findAll(example).collectList();
        contractList
                .subscribe(list -> {
                    if(list.size() > 1){
                        List<Contract> deleteList = list.subList(1, list.size());
                        contractRep.deleteAll(deleteList).subscribe();
                    }
                    //return Mono.empty();
                });

    }

    @Test
    @Order(value = 1)
    public void testContractPublisher() {
        assertNotNull(publisher);
        Contract ct3 = new Contract("ct-103", 10300.23, 50000.01, 10.33);

        Flux.just(ct3)
                .flatMap(contract -> contractService.insert(contract))
                //.ons
                .subscribe(ct -> {
                    log.debug("CONTRACT id amount {} {} ", ct.getContractId(), ct.getAmountDue());
                });

        assertNotNull(ct3);
        reduceContracts("ct-103");

    }

    @Test
    @Order(value = 6)
    public void mapTupleT1() {
        Tuple3<String, Integer, Integer> base = Tuples.of("Foo-York", 200, 300);
        Tuple2<?, ?> mapped = base.mapT1(String::length);
        assertThat(mapped).isNotSameAs(base)
                .hasSize(3)
                .containsExactly(8, base.getT2(), base.getT3());
    }

    @Test
    @Order(value = 5)
    public void testMapTuples() {

        //"ct-102-now-testMapTuples"
        //updateContractByContractId(String contractId, Contract contract)
/*        Contract update = new Contract("ct-102", 202000.02, 190005.00, 12005.00);
        Mono<Contract> backTo102 = contractService.updateContractByContractId("ct-102-now-testMapTuples", update);
        backTo102.subscribe(s -> assertThat(s.getContractId()).isEqualTo("ct-102"));*/

        Mono<Contract> ct2 = contractRep.findByContractId("ct-102");
        Mono<Client> cl1 = clientService.findByClientId("cl-101");
        //TupleUtils


        Mono<Contract> contractMono = Mono.zip(cl1, ct2)
                .map(t -> {
                    t.getT2().getClients().add(t.getT1());
                    t.getT2().setAmountDue(50501.00);
                    //t.getT2().setContractId("ct-102-now-testMapTuples");
                    return t.getT2();
                })
                .flatMap(z -> contractService.saveContract(z));

        StepVerifier.create(contractMono)
                //.expectNextCount(1)
                .assertNext(t -> assertThat(t.getClients().size()).isIn(1, 2, 15))
                .expectComplete()
                .verify();
        //contractMono.subscribe();
        ct2.subscribe();
        cl1.subscribe();
    }

    @Order(value = 6)
    @Test
    public void testTupleStuffAndMore() {


        ExampleMatcher caseInsensitiveExampleMatcher = ExampleMatcher.matchingAll().withIgnoreCase();

        BankingAgg agg = new BankingAgg();
        agg.setAggregateId("agg-test101");

        Example<BankingAgg> example = Example.of(agg);
        Flux<BankingAgg> aggList = aggRepository.findAll(example);
        aggList
                .collectList()
                //.flatMap(z -> aggRepository.deleteAll(z))
                //.count()
                .subscribe(s -> {
                    log.debug("COUNT IS {}", s.size());
                    if(s.size()>0) {
                        aggRepository.deleteAll(s).subscribe();
                    }

                })
        ;

    }

    @Test
    public  void testReduceNoContracts(){

        //reduceContracts("sink-ct-101");

        String contractId = "sink-ct-101";
        Contract contract= new Contract();
        contract.setContractId(contractId);

        Example<Contract> example = Example.of(contract);

        contractRep.delateAllWithContractId(contractId)
                .subscribe(c -> {
                    log.debug("id:: {}", c.getId());
                });

        //contractRep.deleteAll(listFlux).subscribe();
        contractService.countContracts().subscribe(n-> {
            log.debug("deleted cts {}", n.longValue());
                }
        );
    }
}
