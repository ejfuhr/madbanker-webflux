package com.reactive.banker.config;

import com.reactive.banker.entity.*;
import com.reactive.banker.repository.*;
import com.reactive.banker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("data")
@Slf4j
public class FullDataUtilities {

    @Autowired
    private ClientRepository clientRep;

    @Autowired
    private ContractRepository contractRep;

    @Autowired
    private MadBankerRepository bankerRep;
    @Autowired
    private BankingAggRepository aggRepository;
    @Autowired
    private SimpleAggRepository simpleAggRepository;
    @Autowired
    private SimpleAggService simpleAggService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private BankerService bankerService;
    @Autowired
    private PropertyRepository propRep;

    public Mono<BankingAgg> postSingleClientBankerWithPropertiesContracts(){
        Property p1 = new Property(null, "agg-p101", "smith2 st", "abq", "nm", "10101");
        Property p2 = new Property(null, "agg-p102", "snorkle mall", "abq", "nm", "10101");
        List<Property> propertyList = new ArrayList<Property>();
        propertyList.add(p1);
        propertyList.add(p2);
        Flux<Property> propertyFlux = propRep.saveAll(propertyList);
        Mono<List<Property>> propertyMonoList = propertyFlux.collectList();

        Client cl = new Client("agg-clientId");
        cl.setBusinessAddress("101 main");
        cl.setCity("bumkenville");
        cl.setFirstName("bobby tork");
        cl.setLastName("villa");
        cl.setMailCode("10101aa");
        cl.setState("MN");
        Mono<Client> monoClient = clientRep.save(cl);

        Contract ct1 = new Contract("agg-ct-101", 10100.23, 50000.01, 10.33);
        Contract ct2 = new Contract("agg-ct-102", 10600.23, 55000.01, 10.33);
        List<Contract> contractList = new ArrayList<Contract>();
        contractList.add(ct1);
        contractList.add(ct2);

        //Flux<Contract> fluxContract = contractRep.saveAll(contractList);
        Mono<List<Contract>> monoContractList = contractRep.saveAll(contractList).collectList();

        Mono<String> aggId = Mono.just("agg-test101");

        Mono<MadBanker> bk102 = bankerRep.findByBankerId("bk-102");

        Mono<BankingAgg> simpleAgg1 = postAggregateWithSingleClientBanker(
                aggId, monoClient, monoContractList, propertyMonoList,
                bk102
        );

        return simpleAgg1;
    }

    public Mono<BankingAgg> postSingleClientWithBankerContractClientLists(){
        Mono<Client> cl = clientService.findByClientId("agg-clientId");
        Flux<Contract> contractFlux = contractService.getAllContracts();
        Mono<List<Contract>> contractMono = contractFlux.collectList();
        Mono<List<Property>> propertyListMono = propertyService.getAllProperties().collectList();
        Mono<List<MadBanker>> bankerListMono = bankerService.getAllBankers().collectList();
        //simpleAggService.postAggregateWithSingleClientBanker()

        Mono<String> addId = Mono.just("addId-special-999");

        Mono<BankingAgg> agg = simpleAggService.postAggregateWithSingleClient(addId, cl, contractMono,
                propertyListMono,
                bankerListMono);

        cl.subscribe();
        contractMono.subscribe();
        propertyListMono.subscribe();
        bankerListMono.subscribe();
        addId.subscribe();

        return agg;
    }

    public Mono<BankingAgg> postAggregateWithSingleClientBanker(Mono<String> aggId, Mono<Client> clientMono,
                                                                Mono<List<Contract>> contractMonoList,
                                                                Mono<List<Property>> propertyMonoList,
                                                                Mono<MadBanker> bankerMono){
        return simpleAggService.buildAggregateWithSingleClientBanker(aggId, clientMono, contractMonoList, propertyMonoList,
                bankerMono)
                .flatMap(flat -> {
                    LocalDateTime date = LocalDateTime.now();
                    flat.setAddNote(aggId + " with client contracList and propertyList " + date.toString() + " and banker");
                    clientMono.flatMap(clientRep::save);
                    contractMonoList.flatMapMany(contractService::saveAllContracts);
                    propertyMonoList.flatMapMany(propRep::saveAll);
                    bankerMono.flatMap(bankerRep::save);
                    return aggRepository.insert(flat);

                });
    }
}
