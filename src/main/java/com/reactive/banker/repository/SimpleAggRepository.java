package com.reactive.banker.repository;

import com.reactive.banker.entity.BankingAgg;
import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Repository
public interface SimpleAggRepository extends ReactiveMongoRepository<BankingAgg, String> {

    //Mono<BankingAgg> saveByAggregateId(String aggregateId);

    Mono<BankingAgg> findByAggregateId(String aggregateId);

    @Aggregation(pipeline = {
            "{'$match':{'aggregateId': ?0 } }"
    })
    Flux<BankingAgg> findBankingAggsByAggregateId(String aggregateId);
    //addId-special-999

    Mono<Void> deleteByAggregateId(String aggregateId);

    @ExistsQuery("{ aggregateId: ?0 }")
    Mono<Boolean> existsWithAggregateId(String aggregateId);

    @DeleteQuery("{ aggregateId: ?0 }")
    Flux<BankingAgg> delateAllWithAggregateId(String aggregateId);

}
