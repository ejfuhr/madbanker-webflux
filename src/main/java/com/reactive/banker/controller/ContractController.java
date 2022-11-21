package com.reactive.banker.controller;

import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.Property;
import com.reactive.banker.service.ContractService;
import com.reactive.banker.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/contractsController", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class ContractController {

    @Autowired
    private ContractService contractService;
    @Autowired
    private PropertyService propertyService;
    private final MediaType mediaType = MediaType.APPLICATION_JSON;

    @PostMapping(path = "/createContractWithParams")
    //@GetMapping(params = "/createContractWithParams")
    @ResponseBody
    public Mono<ResponseEntity<Contract>> createContractWithRequestParams(
            @RequestParam String contractId,
            @RequestParam Double originalAmount,
            @RequestParam Double amountDue,
            @RequestParam Double interestPaid,
            @RequestParam String propertyId
            ) {
        //public Contract(String
        Contract contract = new Contract(contractId, originalAmount, amountDue, interestPaid);
        Mono<Contract> contractMono = contractService.saveContract(contract);
        Mono<Property> propertyMono = propertyService.findPropertyByPropertyId(propertyId);
        Flux<Contract> contractFlux = Mono.zip(contractMono, propertyMono)
                .flatMapMany(TupleUtils.function(this::combineContractMono));
/*        Mono<List<Contract>> contractListMono = contractFlux.collectList();

        return contractListMono
                .map(p -> ResponseEntity.created(URI.create("/contractFlux/" + p.size()))
                                .contentType(mediaType)
                        .build()

                );*/

        return contractFlux.collectList()
                .map(p -> ResponseEntity
                        //.created(URI.create("/contractFlux/" + p.size()))//getId()))
                        .ok()
                        .contentType(mediaType)
                        .build()
                );

/*        return this.contractService
                .saveContract(contract)
                .map(p -> ResponseEntity.created(URI.create("/contract/" + p.getId()))
                        .contentType(mediaType)

                        .build());*/
    }

    @PostMapping(path = "/createContract")
    @ResponseBody
    public Mono<ResponseEntity<Contract>> create(@RequestBody Contract contract) {
        return this.contractService
                .saveContract(contract)
                .map(p -> ResponseEntity.created(URI.create("/contract/" + p.getId()))
                        .contentType(mediaType)
                        .build());
    }

    @GetMapping(path="/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Mono<Contract> streamContractByContractId(@PathVariable String contractId){
        log.debug("STREAMING>>>>");
        return this.contractService.findByContractId(contractId)
                .filter(contract -> contract.getContractId() == contractId);
    }

    @GetMapping(path = "/allContracts")
    @ResponseBody
    public Publisher<Contract> getAll() {
        return this.contractService.getAllContracts();
    }

/*    @GetMapping("/{id}")
    Publisher<Contract> getById(@PathVariable("id") String id) {
        return this.contractService..get(id);
    }*/

    @GetMapping(path = "/{contractId}")
    @ResponseBody
    public Mono<ResponseEntity<Contract>> getByContractId(@PathVariable("contractId") String contractId) {

        return this.contractService.findByContractId(contractId)
                .map(ResponseEntity::ok)
                .log()
                .defaultIfEmpty(ResponseEntity.ok(new Contract("emptyId"))

                );
    }

    @GetMapping(path = "/contract/{contractId}")
    @ResponseBody
        public Mono<Contract> doByContractId(@PathVariable("contractId") String contractId){
        return this.contractService.findByContractId(contractId)
                .log();
    }

    /**
     * try this "/contractsController/contract/ct-102/cl-101, cl-103/p103, p101/bk-102, bk-103
     * @param contractId
     * @param clientIds
     * @param propertyIds
     * @param bankerIds
     * @return
     */
    @GetMapping(path="/contract/{contractId}/{clientIds}/{propertyIds}/{bankerIds}")
    @ResponseBody
    public Flux<Contract> getFluxContractWIthClientsPropertiesBankers(@PathVariable String contractId,
                                                                      @PathVariable String[] clientIds,
                                                                      @PathVariable String[] propertyIds,
                                                                      @PathVariable String[] bankerIds) {

        return this.contractService.getContractFluxWithContractListsOfClientsPropertiesBankers(contractId, clientIds,
                                                                                             propertyIds, bankerIds);
        //@RequestParam String id
        //@PathVariable String id
    }

    private Mono<Contract> combineContractMono(Contract contractx, Property propx) {
        contractx.getProperties().add(propx);
        return this.contractService.saveContract(contractx);
    }
    //Flux<Contract> getContractFluxWithContractListsOfClientsPropertiesBankers(String contractId, String[] clientIds,
    //                                                                                         String[] propertyIds, String[] bankerIds)

}

