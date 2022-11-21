package com.reactive.banker.service;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.entity.Property;
import com.reactive.banker.repository.ContractRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ContractService {

/*    @Autowired
    private Sinks.Many<Contract> contractSink;*/
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private ContractRepository contractRep;
    @Autowired
    private ClientService clientService;
    @Autowired
    private BankerService bankerService;
    @Autowired
    private PropertyService propertyService;

    public ContractService(ApplicationEventPublisher publisher) {
        super();
        this.publisher = publisher;
    }

    public Flux<Contract> getAllContracts() {
        return contractRep.findAll();
    }

    public Mono<Contract> findByContractId(String contractId) {

        return contractRep.findByContractId(contractId);
    }

    public Mono<Contract> deleteById(String id) {
        return this.contractRep
                .findById(id)
                .flatMap(p -> this.contractRep.deleteById(p.getId()).thenReturn(p));
    }

    public Mono<Contract> deleteByContractId(String contractId) {
        return this.contractRep
                .findByContractId(contractId)
                .flatMap(p -> this.contractRep.deleteByContractId(p.getContractId()).thenReturn(p));
    }

    public Mono<Contract> createContract(String contractId,
                                         Double originalAmount,
                                         Double amountDue,
                                         Double interestPaid) {
        log.debug("creating CONTRACT {} {} {} {} ", contractId, originalAmount, amountDue, interestPaid);
        Contract c = new Contract(contractId, originalAmount, amountDue, interestPaid);
        String s = "success...";
        return contractRep
                .save(c)
/*                .doOnSuccess(lg -> {
                    log.debug("do on success here {} {} ", lg.getContractId(), lg.getContractId());
                })*/
                .doOnSuccess(contract -> this.publisher.publishEvent(new ContractCreatedEvent(contract)));
    }

    public Mono<Contract> saveContract(Contract contract){
        log.debug("saving Contract as object {} {} {} ",
                contract.getId(),
                contract.getContractId(), contract.getAmountDue());
        return  this.contractRep
                .save(contract)
                .doOnSuccess(cnt -> this.publisher.publishEvent(new ContractCreatedEvent(cnt)));
    }
    public Mono<Contract> insert(Contract contract) {
        return this.contractRep
                .insert(contract)
/*                .doOnSuccess(lg -> {
                    log.debug("BEFORE SINK.TRY ME NEXT here {} {} ", lg.getId(), lg.getContractId());
                            this.contractSink.tryEmitNext(lg);
                })*/
                .doOnSuccess(cnt -> this.publisher.publishEvent(new ContractCreatedEvent(cnt)));
    }

    public Mono<Contract> updateContractByContractId(String contractId, Contract contract) {
        return contractRep.findByContractId(contractId)
                .map(ct -> new Contract(contract.getContractId(), contract.getOriginalAmount(), ct.getAmountDue(),
                        ct.getInterestPaid()))
                .flatMap(contractRep::save);
    }

    public Mono<Contract> addPropertyToContractMono(Mono<Property> mp, Mono<Contract> mc){
        return Mono.zip(mp, mc)
                .map(t -> addPropertyToContract(t.getT1(), t.getT2()))
                .flatMap(contractRep::save);
    }

    public Mono<Contract> addPropertyBankerClientToContractMono(Mono<Property> propertyMono, Mono<MadBanker>  bankerMono,
                                                            Mono<Client> clientMono,
                                                            Mono<Contract> contractMono){
        return Mono.zip(propertyMono, bankerMono, clientMono, contractMono)
                .map(t -> addPropertyBankerClientToContract(t.getT1(), t.getT2(), t.getT3(), t.getT4()))
                .flatMap(contractRep::save);

    }

    public Mono<Void> deleteAll() {
        return this.contractRep.deleteAll();
    }

    public Mono<Long> countContracts() {
        return this.contractRep.count();
    }

    public Flux<Contract> saveAllContracts(Iterable<Contract> contractList) {
        return contractRep.saveAll(contractList);
    }

    private Contract addPropertyToContract(Property property, Contract contract) {
        contract.getProperties().add(property);
        return contract;
    }

    private Contract addPropertyBankerClientToContract(Property property, MadBanker banker, Client client,
                                                       Contract contract){
        if(null != property){
            log.debug("adding property >>> {}", property.getPropertyId());
            contract.getProperties().add(property);
        }
        if(null != banker){
            log.debug("adding banker >>> {}", banker.getBankerId());
            contract.getBankers().add(banker);
        }
        if(null != client){
            log.debug("adding client >>> {}", client.getClientId());
            contract.getClients().add(client);
            client.getContracts().add(contract);
            //clientService.updateClientByClientId(client.getClientId(), client);
        }
        return contract;
    }

    /**
     * use testContractListsClientPropertyBankerWithFlatMap from DeconstructByContractTest. Take existing contract,
     * with its contractId, add String arrays of clientIds, propertyIds, bankerIds and update the existing contract.
     * Using the can we pull the property from this with its related client, contracts, bankers etc?
     *
     * @param contractId
     * @param clientIds
     * @param propertyIds
     * @param bankerIds
     * @return
     */
        public Flux<Contract> getContractFluxWithContractListsOfClientsPropertiesBankers(String contractId, String[] clientIds,
                                                                                         String[] propertyIds, String[] bankerIds){
            Mono<Contract> contractMono = this.findByContractId(contractId);
            Mono<List<Client>> clientsListMono = clientService.findClientsWithClientIdsArray(clientIds);
            Mono<List<Property>> propertiesListMono = propertyService.findPropertiesWithPropertyIdsArray(propertyIds);
            Mono<List<MadBanker>> bankersListMono = bankerService.findBankersWithBankerIdsArray(bankerIds);

            Flux<Contract> contractFlux = Mono.zip(contractMono, clientsListMono, propertiesListMono, bankersListMono)
                    .flatMapMany(TupleUtils.function((contrx, clientx, propx, bankerx) ->
                            doContractWithClientPropertyBankerLists(contrx, clientx, propx, bankerx)
                    ));

            return contractFlux;
        }

    public Mono<List<Contract>> findContractsWithContractIdsArray(String[] contractIds){

        List<String> idList = List.of(contractIds);
        List<Mono<Contract>> monoArrayList = new ArrayList<>();
        for (String id : idList) {
            log.debug("iterators id:::{}", id);
            monoArrayList.add(this.findByContractId(id));
        }

        Flux<Contract> contractFlux = Flux.mergeSequential(monoArrayList);
        return  contractFlux.collectList();
    }

    /**
     * from DeconstructByContractTest
     * @param contract
     * @param clientList
     * @param propertyList
     * @param madBankerList
     * @return
     */
    private Mono<Contract> doContractWithClientPropertyBankerLists(Contract contract, List<Client> clientList,
                                                                   List<Property> propertyList,
                                                                   List<MadBanker> madBankerList) {
        contract.getClients().addAll(clientList);
        contract.getProperties().addAll(propertyList);
        contract.getBankers().addAll(madBankerList);
        return this.saveContract(contract);

    }
}