package com.reactive.banker.repository;

import java.util.List;

import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.banker.entity.Contract;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository	
public interface ContractRepository extends ReactiveMongoRepository<Contract, String> {
	
    // > min & < max
    // Flux<Product> findByPriceBetween(int min, int max);
    Flux<Contract> findByAmountDueBetween(Range<Double> range);
    
    Mono<Contract> findByContractId(String contractId);

    Mono<Void> deleteByContractId(String contractId);

    @ExistsQuery("{ contractId: ?0 }")
    Mono<Boolean> existsWithContractId(String contractId);

    @DeleteQuery("{ contractId: ?0 }")
    Flux<Contract> delateAllWithContractId(String contractId);
    
    //findByInterestPaid(Double interestPaid);

}
