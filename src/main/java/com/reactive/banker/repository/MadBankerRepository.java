package com.reactive.banker.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository	
public interface MadBankerRepository extends ReactiveMongoRepository<MadBanker, String> {

	Flux<Contract> findContractsByBankerId(String bankerId);
	
	Mono<MadBanker> findByBankerId(String bankerId);

	Mono<Void> deleteByBankerId(String bankerId);
}
