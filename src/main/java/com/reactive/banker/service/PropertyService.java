package com.reactive.banker.service;

import com.reactive.banker.entity.MadBanker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.reactive.banker.entity.Property;
import com.reactive.banker.repository.PropertyRepository;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PropertyService {
	
	private final ApplicationEventPublisher publisher;
	@Autowired
	private PropertyRepository propRepository;
    public PropertyService(ApplicationEventPublisher publisher) {
		super();
		this.publisher = publisher;
	}

	public Flux<Property> getAllProperties(){
        return this.propRepository.findAll();	
                    //.map(EntityDtoUtil::toDto);
    }

	public Mono<Property> findPropertyByPropertyId(String propertyId){
		return this.propRepository.findByPropertyId(propertyId);

	}

	public Mono<Void> deletePropertyById(String propertyId){

		return this.propRepository.deleteByPropertyId(propertyId);
	}

	public Mono<Void> deleteById(String id){
		return this.propRepository.deleteById(id);
	}
    
    public Mono<Property> create(String propertyId, String streetAddress, String city,
    								String state, String mailCode) { // <7>
    	
    	log.debug("creating {} {} {} {} {} ", propertyId, streetAddress, city,
				state, mailCode);
    	Property p = new Property(null, propertyId, streetAddress, city,
				state, mailCode);
        return this.propRepository
            .save(new Property(null, propertyId, streetAddress, city,
					state, mailCode))
            .doOnSuccess(profile -> this.publisher.publishEvent(new PropertyCreatedEvent(profile)));
    }

	public Mono<Property> insert(Property property){
		return this.propRepository
				.insert(property)
				.doOnSuccess(profile -> this.publisher.publishEvent(new PropertyCreatedEvent(profile)));
	}

	public Mono<Property> updatePropertyByPropertyId(String propertyId, Property updatedProperty){
		log.debug("updating prop id {}", propertyId);
		return propRepository
				.findByPropertyId(propertyId)
				.flatMap(p -> propRepository.save(updatedProperty));
		//this.propRepository.findOne()
	}

	/*
	    public Mono<Profile> update(String id, String email) { // <5>
        return this.profileRepository
            .findById(id)
            .map(p -> new Profile(p.getId(), email))
            .flatMap(this.profileRepository::save);
    }
	 */
/*
	public Mono<Owner> updateByOwnerId(String ownerId, String name, String acronym) {

		return this.ownerRepository
				.findByOwnerId(ownerId)
				.map(p -> new Owner( ownerId, name, acronym))
				.flatMap(this.ownerRepository::save);
	}
*/

	public Mono<Void> deleteAllProperties(){
		return this.propRepository.deleteAll();
	}

	public  Mono<Long> countAllProcuducts(){
		return this.propRepository.count();
	}

	public Mono<List<Property>> findPropertiesWithPropertyIdsArray(String[] propertyIds){

		List<String> idList = List.of(propertyIds);
		List<Mono<Property>> monoArrayList = new ArrayList<>();
		for (String id : idList) {
			log.debug("property iterators id:::{}", id);
			monoArrayList.add(this.findPropertyByPropertyId(id));
		}

		Flux<Property> propertyFlux = Flux.mergeSequential(monoArrayList);
		return  propertyFlux.collectList();
	}


}
