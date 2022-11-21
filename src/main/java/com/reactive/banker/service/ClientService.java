package com.reactive.banker.service;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ClientService {

    private final ApplicationEventPublisher publisher;
    @Autowired
    private ClientRepository clientRepository;

    public ClientService(ApplicationEventPublisher publisher) {
        super();
        this.publisher = publisher;
    }

    public Flux<Client> getAllClients() {
        return this.clientRepository.findAll();
        //.map(EntityDtoUtil::toDto);
    }

    public Mono<Client> findByClientId(String clientId) {
        log.debug(">>>>>> clientId {}", clientId);
        return this.clientRepository.findByClientId(clientId);

    }

    public Mono<Client> deleteById(String id) {
        return this.clientRepository
                .findById(id)
                .flatMap(p -> this.clientRepository.deleteById(p.getId()).thenReturn(p));
    }

    public Mono<Client> deleteByClientId(String clientId) {
        return this.clientRepository
                .findByClientId(clientId)
                .flatMap(p -> this.clientRepository.deleteByClientId(p.getClientId()).thenReturn(p));
    }

    public Mono<Client> createClientAsObject(Client client){
        log.debug("saving Contract as object {} {} {} ",
                client.getClientId(),
                client.getFirstName(),
                client.getLastName());

        return this.clientRepository
                .save(client)
                .doOnSuccess(cnt -> this.publisher.publishEvent(new ClientCreatedEvent(cnt)));
    }
    public Mono<Client> createClient(String clientId,
                                     String firstName,
                                     String lastName,
                                     String businessAddress,
                                     String city,
                                     String state,
                                     String mailCode) {
        log.debug("creating {} {} {} {} {} {} {} ", clientId, firstName, lastName, businessAddress, city,
                state, mailCode);

        Client c = new Client(clientId, firstName, lastName, businessAddress, city,
                state, mailCode);
        return this.clientRepository.save(c)
                .doOnSuccess(profile -> this.publisher.publishEvent(new ClientCreatedEvent(profile)));
    }

    public Mono<Client> addContractToClient(String clientId, Contract contract) {
        return this.clientRepository.findByClientId(clientId)
                .map(cc -> {
                    cc.getContracts().add(contract);
                    return cc;
                })
                .flatMap(this.clientRepository::save);
    }

    public Mono<Client> removeContractToClient(String clientId, Contract contract) {
        return this.clientRepository.findByClientId(clientId)
                .map(cc -> {
                    cc.getContracts().remove(contract);
                    return cc;
                })
                .flatMap(this.clientRepository::save);
    }

    public Mono<Client> updateClientByClientId(String clientId, Client client) {
        Mono<Client> clientMono = clientRepository
                .findByClientId(clientId)
                .map(cl -> new Client(client.getClientId(), client.getFirstName(), client.getLastName(), client.getBusinessAddress(),
                        client.getCity(), client.getState(), client.getMailCode()))
                .flatMap(cl -> clientRepository.save(cl));
        return clientMono;
    }

    public Flux<Contract> getContractListByClientId(String clientId){
        return clientRepository.findAllByClientId(clientId);
    }

    public Flux<Client> saveAllClients(Iterable<Client> clientList){
        return clientRepository.saveAll(clientList);
    }

    public Mono<Void> deleteAll() {
        return this.clientRepository.deleteAll();
    }

    public Mono<Long> countClients() {
        return this.clientRepository.count();
    }

    public Mono<Void> deleteFoystClients(){

        Flux<Client> clientFlux = clientRepository.getFoystClients();
        return clientRepository.deleteAll(clientFlux);
/*        return this.clientRepository.getFoystClients()
            .flatMap(f -> clientRepository.deleteByClientId(f.getClientId()))
                ;*/
    }

    public Mono<List<Client>> findClientsWithClientIdsArray(String[] clientIds){

        List<String> idList = List.of(clientIds);
        List<Mono<Client>> monoArrayList = new ArrayList<>();
        for (String id : idList) {
            log.debug("client iterators id:::{}", id);
            monoArrayList.add(this.findByClientId(id));
        }

        Flux<Client> clientFlux = Flux.mergeSequential(monoArrayList);
        return  clientFlux.collectList();
    }
}
