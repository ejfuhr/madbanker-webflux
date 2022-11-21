package com.reactive.banker.config;

import com.reactive.banker.entity.Contract;
import com.reactive.banker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("classic")
@Slf4j
public class ClassicSetupService implements CommandLineRunner {
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

    @Override
    public void run(String... args) throws Exception {

        log.debug("now in CLASSIC...");

        bankerService.getAllBankers()
                .map(c -> {
                    log.debug("from banker service : {} {} ", c.getBankerId(), c.getContracts().size());
                    return c;
                })
                .subscribe();

        simpleAggService.findAllAggregations()
                .subscribe()
        ;
        simpleAggService.count()
                .map(x -> {
                    log.debug("aggregation count:: " + x);
                    return x;
                })
                .subscribe();

    }
}
