package com.reactive.banker.service;

import com.reactive.banker.entity.*;
import com.reactive.banker.repository.ClientRepository;
import com.reactive.banker.repository.MadBankerRepository;
import com.reactive.banker.repository.PropertyRepository;
import com.reactive.banker.repository.SimpleAggRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple5;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class SimpleAggService {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private BankerService bankerService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private SimpleAggRepository aggRepository;
    @Autowired
    private MadBankerRepository bankerRep;
    @Autowired
    private ClientRepository clientRep;
    @Autowired
    private PropertyRepository propertyRep;

    public SimpleAggService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public Mono<Void> deleteAll(){
        return aggRepository.deleteAll();
    }

    public Mono<BankingAgg> deleteByAggregateId(String aggregateId){
        return aggRepository
                .findByAggregateId(aggregateId)
                .flatMap(bankingAgg -> this.aggRepository.deleteByAggregateId(bankingAgg.getAggregateId())
                        .thenReturn(bankingAgg))
                ;
    }

    public Mono<Void> deleteById(String id){

        return aggRepository.deleteById(id);
    }
    public Flux<BankingAgg> findAllAggregations(){
        return aggRepository.findAll();
    }

    public Mono<Long> count(){
        return aggRepository.count();
    }

    public Mono<BankingAgg> findSingleAggregationByAggId(String aggId){
        return aggRepository.findByAggregateId(aggId);
    }

    public Flux<BankingAgg> getAggregationsByAggId(String aggId){
        return aggRepository.findByAggregateId(aggId).flux();
    }

    public Mono<Boolean> existByAggregateId(String aggregateId){
        return aggRepository.existsWithAggregateId(aggregateId);
    }

    /**
     * First, create a bare BankingAgg, with a String aggregateId and maybe a String addNote. That will be posted (saved)
     * and then this method will do a findByAggregationId.This method needs that aggId with a an existing single contract,
     * and 3 lists of clients, properties, and bankers. This is accomplished by taking stringed id;s as inputs.
     *
     *
     * @param aggId
     * @param contractId
     * @param clientIds
     * @param propertyIds
     * @param bankerIds
     * @return
     */
    public Flux<BankingAgg> getAggregateWithSingleContractPlusLists(String aggId,
                                                                    String contractId,
                                                                    String[] clientIds,
                                                                    String[] propertyIds,
                                                                    String[] bankerIds){
/*        LocalDateTime date = LocalDateTime.now();
        BankingAgg bankingAgg = new BankingAgg();
        bankingAgg.setAggregateId(aggId);
        bankingAgg.setAddNote(aggId + " with single contract and client, property, banker lists " + date.toString());
        Mono<BankingAgg> bankingAggMono = this.aggRepository.save(bankingAgg);*/
        Mono<BankingAgg> bankingAggMono = this.findSingleAggregationByAggId(aggId);
        Mono<Contract> contractMono = contractService.findByContractId(contractId);
        Mono<List<Client>> clientsListMono = clientService.findClientsWithClientIdsArray(clientIds);
        Mono<List<Property>> propertiesListMono = propertyService.findPropertiesWithPropertyIdsArray(propertyIds);
        Mono<List<MadBanker>> bankersListMono = bankerService.findBankersWithBankerIdsArray(bankerIds);

        Flux<BankingAgg> bankingAggFlux = Mono.zip(bankingAggMono, contractMono, clientsListMono, propertiesListMono, bankersListMono)
                .flatMapMany(TupleUtils.function((aggx, contrx, clientx, propx, bankerx) ->
                        doAggregateWithContractPlusLists(aggx, contrx, clientx, propx, bankerx)
                ));
        return bankingAggFlux;

    }

    public Flux<BankingAgg> postNewAggregateWithSingleClientPlusLists(String aggId,
                                                               String clientId,
                                                                String[] contractIds,
                                                               String[] propertyIds,
                                                               String[] bankerIds){
        //create simpleBankingAgg
        LocalDateTime date = LocalDateTime.now();
        BankingAgg bankingAgg = new BankingAgg();
        bankingAgg.setAggregateId(aggId);
        bankingAgg.setAddNote(aggId + " with single client and contract, property, banker lists " + date.toString());
        Mono<BankingAgg> bankingAggMono = this.aggRepository.save(bankingAgg);
        //find client
        Mono<Client> clientMono = this.clientService.findByClientId(clientId);
        //find contracts
        Mono<List<Contract>> contractsListMono = this.contractService.findContractsWithContractIdsArray(contractIds);
        //find properties
        Mono<List<Property>> propertiesListMono = propertyService.findPropertiesWithPropertyIdsArray(propertyIds);
        //and bankers
        Mono<List<MadBanker>> bankersListMono = bankerService.findBankersWithBankerIdsArray(bankerIds);
        Flux<BankingAgg> bankingAggFlux = Mono.zip(bankingAggMono, clientMono, contractsListMono, propertiesListMono, bankersListMono)
                .flatMapMany(TupleUtils.function((aggx, clientx, contractsx, propsx, bnakersx) ->
                                doAggregateWithClientPlusLists(aggx, clientx, contractsx, propsx, bnakersx)
                        ));

        return bankingAggFlux;
    }

    public Flux<BankingAgg> deleteAllByAggId(String aggId){
        return this.aggRepository.delateAllWithAggregateId(aggId);
    }

/*    @ExistsQuery("{ aggregateId: ?0 }")
    Mono<Boolean> existsWithAggregateId(String aggregateId);

    @DeleteQuery("{ aggregateId: ?0 }")
    Flux<BankingAgg> delateAllWithAggregateId(String aggregateId);*/
    public Mono<BankingAgg> postAggregateWithSingleClient(Mono<String> aggId, Mono<Client> clientMono,
                                                          Mono<List<Contract>> contractMonoList,
                                                          Mono<List<Property>> propertyMonoList,
                                                          Mono<List<MadBanker>> bankerMonoList){

        return buildAggregateWithSingleClient(aggId, clientMono, contractMonoList, propertyMonoList,
                bankerMonoList)
                .flatMap(f -> {
                    LocalDateTime date = LocalDateTime.now();
                    f.setAddNote(aggId + " with client and contract, property, banker lists " + date.toString());
                    clientMono.flatMap(clientRep::save);
                    contractMonoList.flatMapMany(contractService::saveAllContracts);
                    propertyMonoList.flatMapMany(propertyRep::saveAll);
                    bankerMonoList.flatMapMany(bankerRep::saveAll);
                    return aggRepository.insert(f);
                });
    }



    public Mono<BankingAgg> saveBankingApp(BankingAgg bankingApp){

        return aggRepository.save(bankingApp)
                .doOnSuccess(bankingAgg -> this.publisher.publishEvent(new SimplaAggCreatedEvent(bankingAgg)));
    }


    public Mono<BankingAgg> aggregate(String aggregateId, String clientId, String contractId,
                                      String bankerId,
                                      String propertyId){
        return Mono.zip(
                        Mono.just(aggregateId),
                        this.clientService.findByClientId(clientId),
                        this.contractService.findByContractId(contractId),
                        this.bankerService.findBankerByBankerId(bankerId),
                        this.propertyService.findPropertyByPropertyId(propertyId)
                )
                .map(t -> toData(t.getT1(), t.getT2(), t.getT3(), t.getT4(), t.getT5()));
    }

    public Mono<BankingAgg> buildAggregateWithSingleClientBanker(Mono<String> aggId, Mono<Client> clientMono,
                                                                 Mono<List<Contract>> contractMonoList,
                                                                 Mono<List<Property>> propertyMonoList,
                                                                 Mono<MadBanker> bankerMono) {

        log.debug("IN buildAggregateWithSingleClientBanker....");
        return Mono.zip(aggId, clientMono, contractMonoList, propertyMonoList, bankerMono)
                .map(this::combine);
    }

    public Mono<BankingAgg> buildAggregateWithSingleClient(Mono<String> aggId,
                                                            Mono<Client> clientMono,
                                                            Mono<List<Contract>> contractMonoList,
                                                            Mono<List<Property>> propertyMonoList,
                                                            Mono<List<MadBanker>> bankerMonoList
                                                            ){
        return Mono.zip(aggId, clientMono, contractMonoList, propertyMonoList, bankerMonoList)
                .map(this::combineAllToClient);
    }
    /*
    aggregateId, Client client, List<Contract> contractList, List<Property> propertyList,
                      List<MadBanker> bankerList
     */

    private BankingAgg combineAllToClient(Tuple5<String, Client, List<Contract>, List<Property>, List<MadBanker>> tuple) {
        log.debug("IN combineAllToClient...");
        return new BankingAgg(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3(),
                tuple.getT4(),
                tuple.getT5()
        );
    }

    private BankingAgg combine(Tuple5<String, Client, List<Contract>, List<Property>, MadBanker> tuple) {
        log.debug("IN combine....");
        return new BankingAgg(
                tuple.getT1(),
                tuple.getT2(),
                tuple.getT3(),
                tuple.getT4(),
                tuple.getT5()
        );

    }


    private BankingAgg toData(String aggregateId, Client client, Contract contract, MadBanker madBanker,
                              Property property){

        return new BankingAgg(aggregateId, client, contract, madBanker, property);
    }

    private Mono<BankingAgg> doAggregateWithContractPlusLists(BankingAgg bankingAgg,
                                                              Contract contract,
                                                              List<Client> clientList,
                                                              List<Property> propertyList,
                                                              List<MadBanker> madBankerList) {
        bankingAgg.getContractList().add(contract);
        bankingAgg.getClientList().addAll(clientList);
        bankingAgg.getPropertyList().addAll(propertyList);
        bankingAgg.getBankerList().addAll(madBankerList);
        return this.saveBankingApp(bankingAgg);

    }

    private Mono<BankingAgg> doAggregateWithClientPlusLists(BankingAgg bankingAgg,
                                                              Client client,
                                                              List<Contract> contractList,
                                                              List<Property> propertyList,
                                                              List<MadBanker> madBankerList) {
        bankingAgg.setClient(client);
        bankingAgg.getContractList().addAll(contractList);
        bankingAgg.getPropertyList().addAll(propertyList);
        bankingAgg.getBankerList().addAll(madBankerList);
        return this.saveBankingApp(bankingAgg);

    }
}
