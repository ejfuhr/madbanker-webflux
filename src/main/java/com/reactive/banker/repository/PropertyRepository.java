package com.reactive.banker.repository;

import org.springframework.data.domain.Range;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactive.banker.entity.Property;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository	
public interface PropertyRepository extends ReactiveMongoRepository<Property, String> {
	
    // > min & < max
    // Flux<Product> findByPriceBetween(int min, int max);
    //Flux<Property> findByPriceBetween(Range<Integer> range);
	
	Mono<Property> findByPropertyId(String propertyId);

	Mono<Void> deleteByPropertyId(String propertyId);



}
