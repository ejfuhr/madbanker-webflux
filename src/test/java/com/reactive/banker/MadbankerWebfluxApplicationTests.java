package com.reactive.banker;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.entity.Property;
import com.reactive.banker.repository.ClientRepository;
import com.reactive.banker.repository.ContractRepository;
import com.reactive.banker.repository.MadBankerRepository;
import com.reactive.banker.repository.PropertyRepository;
import com.reactive.banker.service.ClientService;
import com.reactive.banker.service.ContractService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@SpringBootTest
@EnableMongoRepositories
@ActiveProfiles("classic")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MadbankerWebfluxApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(MadbankerWebfluxApplicationTests.class);
    @Autowired
    //staticxfile.txt
    ReactiveMongoTemplate template;

    @Autowired
    private PropertyRepository propRep;

    @Autowired
    private ClientRepository clientRep;

    @Autowired
    private ContractRepository contractRep;

    @Autowired
    private MadBankerRepository bankerRep;

    @Autowired
    private ContractService contractService;
    @Autowired
    private ClientService clientService;

    @BeforeAll
    public void deleteExtras(){

        log.debug("in beforeAll...");
        //this.clientService.deleteFoystClients();

    }
    //@BeforeEach
    public void prep() {
        if (!template.collectionExists(Property.class).block()) {
            log.debug("creating property");
            template.createCollection(Property.class);
        }
        if (!template.collectionExists(Client.class).block()) {
            log.debug("creating client");
            template.createCollection(Client.class);
        }

        if (!template.collectionExists(Contract.class).block()) {
            log.debug("creating contract");
            template.createCollection(Contract.class);
        }

        if (!template.collectionExists(MadBanker.class).block()) {
            log.debug("creating madBanker");
            template.createCollection(MadBanker.class);
        }
    }

    private void prepareCollections() {
        var propCollection = template.collectionExists(Property.class)
                .flatMap(exists -> exists ? template.dropCollection(Property.class) : Mono.just(exists))
                .then();


        propCollection.as(StepVerifier::create).expectNextCount(0).verifyComplete();

        Client cl = new Client("spec-clientId");
        Mono<Client> special = Mono.just(cl);

        var clientCollection = template.collectionExists(Client.class)
                .flatMap(exists -> exists ? template.save(special) : Mono.just(special))
                .then();

        clientCollection.as(StepVerifier::create).expectNextCount(0).verifyComplete();

        var contractCollection = template.collectionExists(Contract.class)
                .flatMap(exists -> exists ? template.dropCollection(Contract.class) : Mono.just(exists))
                .then();

        var bankerCollection = template.collectionExists(MadBanker.class)
                .flatMap(exists -> exists ? template.dropCollection(MadBanker.class) : Mono.just(exists))
                .then();

        contractCollection.as(StepVerifier::create).expectNextCount(0).verifyComplete();
        bankerCollection.as(StepVerifier::create).expectNextCount(0).verifyComplete();
    }

    @Test
    @Order(value = 1)
    void addPropertyToContractTest() {
        //prepareCollections();

        Mono<Contract> ct1 = contractRep.findByContractId("ct-101");
        Mono<Property> p1 = propRep.findByPropertyId("p103");

        Mono<Contract> addPropertyToContractMono = contractService.addPropertyToContractMono(p1, ct1);
        ct1.subscribe();
        p1.subscribe();
        StepVerifier.create(addPropertyToContractMono)
                .expectNextCount(1)
                //.expectNext(c)
                .verifyComplete();
    }

    @Test
    @Order(value = 2)
        //@Disabled
    void findByPropId() {

        Mono<Property> p1 = propRep.findByPropertyId("p101");
        StepVerifier.create(p1).assertNext(pub -> {
            log.debug("here's p101 stuff {} {}", pub.getPropertyId(), pub.getStreetAddress());
            assertEquals("p101", pub.getPropertyId());
            assertEquals("101 smith st", pub.getStreetAddress());
            assertNotNull(pub.getId());
        }).expectComplete().verify();

        //propRep.deleteByPropertyId("p101");
    }

    /*
    addPropertyBankerClientToContract(Property property, MadBanker banker, Client client,
                                                       Contract contract)
     */
    @Test
    @Order(value = 3)
    void addPropertyBankerClientToContractTest() {

        Mono<Contract> ct2 = contractRep.findByContractId("ct-102");

        Mono<Property> p1 = propRep.findByPropertyId("p101");
        Mono<MadBanker> b102 = bankerRep.findByBankerId("bk-102");
        Mono<Client> cl1 = clientService.findByClientId("cl-101");

        cl1.map(c -> {
            assertEquals("cl-101", c.getClientId());
            return c;
        });

        Mono<Contract> addPropertyBankerClient = contractService.addPropertyBankerClientToContractMono(p1, b102, cl1, ct2);
        //complete all monnos
        ct2.subscribe();
        p1.subscribe();
        b102.subscribe();
        cl1.subscribe();

        StepVerifier.create(addPropertyBankerClient)
                .assertNext(pub -> {
                    log.debug("do we have a contract?? {} {} {} {}", pub.getContractId(), pub.getProperties().size(),
                            pub.getBankers().size(), pub.getClients().size());
                    //assertEquals("p101", pub.getProperties().get(0).getPropertyId());
                    //assertNotEquals("cl-101", pub.getClients().get(0).getClientId(), "no client here");
                })

                .verifyComplete();

    }

    @Order(value = 4)
    @Test
    void checkClients() {
        Mono<Long> clientCt = clientService.countClients();
        StepVerifier.create(clientCt)
                .assertNext(n -> assertTrue(n >= 4, "what up"))
                .verifyComplete();

        Mono<Contract> ct2 = contractRep.findByContractId("ct-102");
        //add client 103
        Mono<Client> cl3 = clientService.findByClientId("cl-103");
        ct2.subscribe();
        cl3.subscribe();

        Mono<Contract> contractMono = Mono.zip(cl3, ct2)
                .map(t -> {
                    t.getT2().getClients().add(t.getT1());
                    return t.getT2();
                })
                .flatMap(z -> contractService.saveContract(z));

        StepVerifier.create(contractMono)
                .assertNext(c -> assertThat(c.getClients().get(0).getClientId()).isEqualTo("cl-103"))
                .expectComplete()
                .verify();
        //.expectNextCount(0)
        //.verifyComplete();
    }

    @Order(value = 5)
    @Test
    //@Disabled
    public void testTupleUtilsAddToContract() {

        Mono<Contract> ct2 = contractRep.findByContractId("ct-102");
        Mono<Property> p1 = propRep.findByPropertyId("p101");
        Mono<MadBanker> b102 = bankerRep.findByBankerId("bk-102");
        Mono<Client> cl1 = clientService.findByClientId("cl-101");
        Long no = clientService.countClients().block();
        Mono<Client> cl2 = clientService.createClient("cl-goofy" + no,
                                    "foyst",
                                    "laname" + no,
                                    "mean st " + no *2,
                                    "dotty",
                                    "nm",
                                    "10102");


        Mono<Contract> localZip =
                Mono.zip(p1, b102, cl1, cl2, ct2)
                        .map(tuple -> {
                            Property p = tuple.getT1();
                            MadBanker b = tuple.getT2();
                            Client cl = tuple.getT3();
                            Client clx2 = tuple.getT4();
                            Contract ct = tuple.getT5();
                            ct.getProperties().add(p);
                            ct.getBankers().add(b);
                            ct.getClients().add(cl);
                            ct.getClients().add(clx2);
                            //return contractService.insert(ct);
                            return ct;
                        })
                        .flatMap(f -> {
                            ;
                            Client client = f.getClients().get(0);
                            client.getContracts().add(f);
                            log.debug("clientId and NAME {} {} ", client.getClientId(), client.getFirstName());
                            clientService.updateClientByClientId(client.getClientId(), client);
                            return contractService.saveContract(f);
                        });

        StepVerifier.create(localZip)
                //.assertNext(c -> assertThat(c.getClients().get(0).getClientId()).isEqualTo("cl-101"))
                //.expectNextCount(1)
                .assertNext(c -> assertThat(c.getClients().size()).isEqualTo(2))

                //.expectComplete()
                .verifyComplete();
        //localZip.subscribe();
    }

    private Mono<Contract> addPropertyToContractMono(Mono<Property> mp, Mono<Contract> mc) {
        return Mono.zip(mp, mc)
                .map(t -> addPropertyToContract(t.getT1(), t.getT2()))
                .flatMap(contractRep::save);
    }

    private Contract addPropertyToContract(Property property, Contract contract) {
        contract.getProperties().add(property);
        return contract;
    }

    private static class uiList {

        public static Mono<Property> show(Property p) {
            log.debug("********** here's showing property {}", p.toString());
            return null;
        }
    }

    private Mono<Property> getDetails(String id) {
        return null;
    }

}
