package com.reactive.banker.config;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.entity.Property;
import com.reactive.banker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

//@Service
@Configuration
@Profile("data")
@Slf4j
public class FullDataSetupService implements CommandLineRunner {

    @Autowired
    private BankerService bankerService;

    @Autowired
    private ClientService clientService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private PropertyService propertyService;
    @Autowired
    private SimpleAggService simpleAggService;
    @Autowired
    private FullDataUtilities utilities;


    @Override
    public void run(String... args) throws Exception {

        log.debug("deleting all data...");
        deleteAllData().subscribe();

        Property p1 = new Property(null, "p101", "101 smith st", "abq", "nm", "10101");
        Property p2 = new Property(null, "p102", "102 smith st", "abq", "nm", "10101");
        Property p3 = new Property(null, "p103", "104b smith2 st", "abq", "nm", "10101");
        Property p4 = new Property(null, "p104", "105b smith2 st", "abq", "nm", "88505");
        Property p5 = new Property(null, "p105", "105 wrong side", "street", "nm", "87999");
        Property p6 = new Property(null, "p106", "106b minor ave", "abq", "nm", "87124");
        Property p7 = new Property(null, "p107", "107-11 bad store blvd", "abq", "nm", "87124");

        Flux.just(p1, p2, p3, p4, p5, p6, p7)
                .flatMap(p -> propertyService.insert(p))
                .subscribe(prop -> {
                    System.out.println("prop:: " + prop.toString());
                });


        Contract ct1 = new Contract("ct-101", 10100.23, 50000.01, 10.33);
        Contract ct2 = new Contract("ct-102", 10200.23, 50000.01, 10.33);
        Contract ct3 = new Contract("ct-103", 10300.23, 50000.01, 10.33);
        Contract ct4 = new Contract("ct-104", 10300.23, 50000.01, 10.33);
        Contract ct5 = new Contract("ct-105", 10300.23, 50000.01, 10.33);
        List<Contract> contractList = List.of(ct1, ct2, ct3, ct4, ct5);

        Flux<Contract> fluxContracts = contractService.saveAllContracts(contractList);
        fluxContracts
                .map(ctr -> {
                    ctr.getProperties().add(p1);
                    contractService.updateContractByContractId(ctr.getContractId(), ctr);
                    return ctr;
                })
                .subscribe(s -> log.debug("CONTRACT {} {} ", s.getContractId(), s.getProperties().size()));
                //.subscribeOn( Schedulers.boundedElastic())

        Client cl1 = new Client("cl-101", "apple", "headed",  "101 charlie st",
                "charlie", "va", "20202");
        Client cl2 = new Client("cl-102", "pickle", "lamont",  "101 charlie st",
                "charlie", "va", "20202");
        Client cl3 = new Client("cl-103", "lucy", "lamore",  "101 charlie st",
                "charlie", "va", "20202");
        Client cl4 = new Client("cl-104", "tarzan", "imperialist 3rd",  "101 charlie st",
                "charlie", "va", "20202");
        List<Client> clientList = List.of(cl1,cl2, cl3, cl4);

        Flux<Client> clientFlux = clientService.saveAllClients(clientList);
        clientFlux.subscribe(c -> {
                    log.debug("CLIENT HERE:: {} {} {} ", c.getClientId(), c.getFirstName(), c.getState());
                });


        MadBanker mb1 = new MadBanker("bk-101",
                "smedley x",
                "butler",
                "10101");

        MadBanker mb2 = new MadBanker("bk-102",
                "smedley",
                "langley the 3rd",
                "10101zz");

        MadBanker mb3 = new MadBanker("bk-103",
                "jones",
                "langley",
                "10101zz");
        MadBanker mb4 = new MadBanker("bk-104",
                "monopoly",
                "mugster",
                "90909");

        List<MadBanker> madBankerList = List.of(mb1, mb2, mb3, mb4);
        Flux<MadBanker> madBankerFlux = bankerService.saveAllBankers(madBankerList);
        madBankerFlux.subscribe(b -> {
            log.debug("saving bankers {} {} ", b.getBankerId(), b.getNickName(), b.getLastName());
        });

        utilities.postSingleClientBankerWithPropertiesContracts()
                .subscribe(s -> {
                    log.debug("saving BankingAgg - clientBankerWithPropClients {} {} {} ",
                            s.getAggregateId(), s.getClient().getLastName(), ct1.getProperties().size());
                });
        utilities.postSingleClientWithBankerContractClientLists()
                .subscribe(s -> {
                    log.debug("saving BankingAgg with singleClientWithBankerContractClientLists {} {} {}",
                            s.getAggregateId(), s.getClient().getClientId(), s.getBankerList().size());
                });


    }

    private Mono<Void> deleteAllData(){
        return propertyService.deleteAllProperties()
                .then(bankerService.deleteAll())
                .then(clientService.deleteAll())
                .then(contractService.deleteAll())
                .then(simpleAggService.deleteAll());
    }

    private void emptyList(List list){
        Iterator it = list.listIterator();
        while (it.hasNext()){
            it.remove();
        }
    }
}
