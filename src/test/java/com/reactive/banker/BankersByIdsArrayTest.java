package com.reactive.banker;

import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.repository.*;
import com.reactive.banker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnableMongoRepositories
@ActiveProfiles("classic")
@Slf4j
public class BankersByIdsArrayTest {
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

    @Test
    public void testBankersThruIds() {
        String[] ids = {"bk-101", "bk-102", "bk-103"};
        List<String> idList = List.of(ids);
        //for(int i = idList.size()){
        int i = idList.size();

        List<Mono<MadBanker>> bs = new ArrayList<>();
        Iterator<String> it = idList.listIterator();
        while (it.hasNext()) {
            String id = it.next();
            log.debug("iterators id:::{}", id);
            bs.add(bankerService.findBankerByBankerId(id));
        }


        //bs.add(bankerService.findBankerByBankerId(idList.get(1)));

        Flux<MadBanker> bankerFlux = Flux.mergeSequential(bs);

        StepVerifier.create(bankerFlux)
                .assertNext(s -> s.getBankerId().equals("bk-101"))
                .expectNextCount(2)
                .verifyComplete();
        //bankerFlux.subscribe();


        Mono<List<MadBanker>> blist = bankerFlux.collectList();
        blist
                .subscribe(s -> {
                            assertThat(s.size()).isEqualTo(3);
                            log.debug("ID::: {}", s.get(0).getBankerId());
                        }
                );

        //assertThat(b.getBankerId()).isEqualTo("bk-101"));
        //addBankersToBankerList(new String[]{"bk-101", "bk-102", "bk-103"});
    }

    @Test
    public void addBankersToBankerList() {

        String[] bankerIds = {"bk-101", "bk-102", "bk-103"};
        //add bankers bk-101 bk-102, bk-103
        //Mono<MadBanker> banker = Mono.just();
        Flux<MadBanker> bankerFlux = Flux.just();
        List<String> bankerIdList = List.of(bankerIds);

        Mono<MadBanker> banker = bankerService.findBankerByBankerId(bankerIds[0]);
        Mono<MadBanker> banker2 = bankerService.findBankerByBankerId(bankerIds[1]);
        assertThat(banker).isNotNull();
        bankerFlux
                .mergeWith(banker2);

        StepVerifier.create(bankerFlux)
                //.assertNext(b -> assertThat(b.getNickName()).isNotNull())
                //.expectNextCount(1)
                .verifyComplete();

    }
}
