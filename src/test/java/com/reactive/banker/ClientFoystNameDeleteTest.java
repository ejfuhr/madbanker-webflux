package com.reactive.banker;

import com.reactive.banker.entity.Client;
import com.reactive.banker.repository.ClientRepository;
import com.reactive.banker.repository.ContractRepository;
import com.reactive.banker.repository.MadBankerRepository;
import com.reactive.banker.repository.PropertyRepository;
import com.reactive.banker.service.ClientService;
import com.reactive.banker.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.mongodb.assertions.Assertions.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableMongoRepositories
@ActiveProfiles("classic")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class ClientFoystNameDeleteTest {

    @Autowired
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
    public void deleteExtras() {

        log.debug("in beforeAll...");
        //this.clientService.deleteFoystClients();

    }

    @Order(value = 1)
    @Test
    void checkClientsAgain() {
        Mono<Long> clientCt = clientService.countClients();
        StepVerifier.create(clientCt)
                .assertNext(n -> assertTrue(n >= 4, "what up"))
                .verifyComplete();

    }


    @Order(value = 2)
    @Test
    void testDeleteTheFoystsAgain() {
        //this.clientService.deleteFoystClients();
        Flux<Client> foysts = clientRep.findClientsByFirstNameAndMailCode("foyst", "10102");

        foysts.collectList()
                .flatMap(f -> {
                    List<Client> list = f;
                    while (f.iterator().hasNext()) {
                        Client c = f.iterator().next();
                        log.debug("TTTTTTTT");
                        assertNull(c);
                    }
                    log.debug("flatmap {} {}", f.size());
                    return Mono.just(f);
                });
        foysts.subscribe(s -> {
            int n = 0;
            log.debug("SUBSCRIBE {} {} ", s.getFirstName(), n++);
            deleteMyFoystName(s);
        });


        StepVerifier.create(clientService.countClients())
                .assertNext(n -> assertThat(n).isGreaterThanOrEqualTo(4))
                .verifyComplete();
    }

    private void deleteMyFoystName(Client client) {
        clientRep.delete(client)
                .subscribe();
        clientRep.count()
                .map(c -> {
                    log.debug("count {}");
                    return c;
                })
                .subscribe();
    }
}
