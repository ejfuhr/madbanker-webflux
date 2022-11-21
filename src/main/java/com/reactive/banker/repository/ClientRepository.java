package com.reactive.banker.repository;

import com.reactive.banker.entity.BankingAgg;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.Property;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.banker.entity.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository	
public interface ClientRepository extends ReactiveMongoRepository<Client, String >{

    Mono<Client> findByClientId(String clientId);

    Mono<Void> deleteByClientId(String clientId);

    Flux<Contract> findAllByClientId(String clientId);

    @Aggregation(pipeline = {"  { '$match': { 'firstName' : { $in: ['foyst']  } } }" })
    Flux<Client> getFoystClients();

    @Aggregation(pipeline = {
            "{'$match':{'firstName': ?0, 'mailCode' : ?1 } }"
    })
    Flux<Client> findClientsByFirstNameAndMailCode(String firstName, String mailCode);

}
