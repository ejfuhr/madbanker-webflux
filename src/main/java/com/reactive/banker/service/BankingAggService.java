package com.reactive.banker.service;

import com.reactive.banker.entity.*;
import com.reactive.banker.repository.BankingAggRepository;
import com.reactive.banker.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class BankingAggService {

    @Autowired
    private BankingAggRepository bankingAggRepository;

    private  Mono<Void> deleteAll(){
        return bankingAggRepository.deleteAll();
    }

    public Mono<BankingAgg> buildISingleClientWithContractsProperties(Mono<String> aggId,
                                                                      Mono<Client> clientMono,
                                                   Mono<List<Contract>> contractMonoList,
                                                   Mono<List<Property>> propertyMonoList) {
        return Mono.zip(aggId, clientMono, contractMonoList, propertyMonoList)
                .map(this::combineForSingleClientContractsProperties);
    }

    public Mono<BankingAgg> buildSingleClientWithContractsBankersProperties(Mono<String> aggId,
                                                                            Mono<Client> clientMono,
                                                                            Mono<List<Contract>> contractMonoList,
                                                                            Mono<List<Property>> propertyMonoList,
                                                                            Mono<List<MadBanker>> bankerMonoList){
        return Mono.zip(aggId, clientMono, contractMonoList,  propertyMonoList, bankerMonoList)
                .map(this::combineForSingleClientBankers);

    }

    private BankingAgg combineForSingleClientContractsProperties(Tuple4<String, Client, List<Contract>, List<Property>> tuple) {
        return new BankingAgg(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3(),
                tuple.getT4()
        );
    }

    private BankingAgg combineForSingleClientBankers(Tuple5<String, Client, List<Contract>,
                 List<Property>, List<MadBanker>> tuple) {
        return new BankingAgg(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3(),
                tuple.getT4(),
                tuple.getT5()
        );
    }
}
