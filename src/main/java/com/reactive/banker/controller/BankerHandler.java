package com.reactive.banker.controller;

import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.service.BankerService;
import com.reactive.banker.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RenderingResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import javax.print.DocFlavor;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;


@Component
@Slf4j
public class BankerHandler {


    private BankerService bankerService;

    @Autowired
    private ContractService contractService;

    public  BankerHandler(@Autowired BankerService bankerService){
        super();
        this.bankerService = bankerService;
    }

    public Mono<ServerResponse> createBanker(ServerRequest req){
        Mono<MadBanker> banker = req.bodyToMono(MadBanker.class);
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        fromPublisher(banker.flatMap(bankerService::saveBanker
                        ), MadBanker.class)
                )
                ;
    }

    public Mono<ServerResponse> deleteBankerById(ServerRequest request) {
        Mono<MadBanker> bankerMono = bankerService.deleteById(request.pathVariable("id"));
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bankerMono, MadBanker.class);
    }

    public Mono<ServerResponse> deleteBankerByBankerId(ServerRequest request) {
        Mono<MadBanker> bankerMono = bankerService.deleteByBankerId(request.pathVariable("bankerId"));
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bankerMono, MadBanker.class);
    }


    public Mono<ServerResponse> getBankerByBankerId(ServerRequest req){
        log.debug( "METHOD & PATH {} {} ", req.method(), req.path() );
        String bankerId = req.pathVariable("bankerId");

        Mono<MadBanker> banker = bankerService.findBankerByBankerId(bankerId);
        return banker
                .flatMap(bkr -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)

                        .body(fromPublisher(banker, MadBanker.class))
                        .switchIfEmpty(ServerResponse.notFound().build())
                );
    }

    public Mono<ServerResponse> getAllBankers(ServerRequest req) {
        return
                ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fromPublisher(bankerService.getAllBankers(), MadBanker.class))
                ;
    }

    public Mono<ServerResponse> updateBankerByBankerId(ServerRequest req){
        String bankerId = req.pathVariable("bankerId");
        String lastName = req.pathVariable("lastName");
        String nickName = req.pathVariable("nickName");
        String mailCode = req.pathVariable("mailCode");
        String[] contractsArray = StringUtils.split(req.pathVariable("contracts"), ",");

        log.debug( "METHOD & PATH IN PUT {} {} ", req.method(), req.path() );
        log.debug("bankerID {} lastName{} nickName {} mai;Code {} contractsArray {} ", bankerId,
                lastName, nickName, mailCode, contractsArray);
      //bodyToMono(new ParameterizedTypeReference<List<AccountOrder>>() {})
        Mono<MadBanker> bankerMono = bankerService.findBankerByBankerId(bankerId)
                .map(c -> new MadBanker(c.getId(),bankerId, lastName, nickName, mailCode))
                .flatMap(b -> this.bankerService.updateBankerByBankerIdWithContractList(bankerId, b))
                ;
        Mono<List<Contract>> contractsListMono = contractService.findContractsWithContractIdsArray(contractsArray);
        Flux<MadBanker> madBankerFlux1 = Mono.zip(bankerMono, contractsListMono)
                .flatMapMany(TupleUtils.function((bankerx, contractx) ->
                                doBankWithContractList(bankerx, contractx)
                        )
                );
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(madBankerFlux1, MadBanker.class);

/*

        Flux<MadBanker> madBankerFlux = req.bodyToFlux(MadBanker.class)
                .flatMap(p -> {
                    MadBanker m = new MadBanker(bankerId, p.getLastName(), p.getNickName(), p.getMailCode());
                    log.debug( "banker to string {} {} ", m.toString() );
                    return bankerService.updateBankerByBankerId(bankerId,m);
                        }
                );
        Mono<ServerResponse> msr = ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(madBankerFlux, MadBanker.class)
                ;
        return msr;*/
    }

    public Mono<ServerResponse> updateByBankerBody(ServerRequest req){

        Flux<MadBanker> bankerFlux = req.bodyToFlux(MadBanker.class)
                .flatMap(b -> this.bankerService.updateByBankerBody(req.pathVariable("bankerId"), b)
                )
                ;
        Mono<ServerResponse> serverResponseMono = ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bankerFlux, new ParameterizedTypeReference<MadBanker>() {
                })
                //.map(this::printMethod)
                //.body(bankerFlux, MadBanker.class)
                ;
        return serverResponseMono;
    }

    public Mono<ServerResponse> updateBankerWithQueryParams(ServerRequest req){
        String bankerId = req.queryParam("bankerId").get();
        String contractIds = req.queryParam("contracts").get();
        String[] cIds = StringUtils.split(contractIds, ",");
        log.debug(":::::::: contractIds cIds ::::{} {} ", contractIds, cIds);
        Mono<MadBanker> bankerMono = bankerService.findBankerByBankerId(bankerId);
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bankerMono, MadBanker.class)
                ;
    }

    /**
     * this hould be in BankerService - here for giggles now
     * @param madBanker
     * @param contractList
     * @return
     */

    private Mono<MadBanker> doBankWithContractList(MadBanker madBanker, List<Contract> contractList){
        madBanker.getContracts().addAll(contractList);
        return this.bankerService.saveBanker(madBanker);
    }


/*    private ServerResponse printMethod(Flux<MadBanker> mb){
        System.out.println(mb);
        return mb;
    }*/

    /*
    String bankerId;
	private String lastName;
	private String nickName;
	private String mailCode;
	private List<Contract> contracts;
     */

    /*
    update that works. service:
        public Mono<Profile> update(String id, String email) { // <5>
        return this.profileRepository
            .findById(id)
            .map(p -> new Profile(p.getId(), email))
            .flatMap(this.profileRepository::save);
    }

        Mono<ServerResponse> updateById(ServerRequest r) {
        Flux<Profile> id = r.bodyToFlux(Profile.class)
            .flatMap(p -> this.profileService.update(id(r), p.getEmail()));
        return defaultReadResponse(id);
    }

    	Mono<ServerResponse> updateById(ServerRequest r) {
			Flux<Property> id = r.bodyToFlux(Property.class)
				.flatMap(p -> this.propertyService.updateByPropertyId(propertyId(r),
						p.getPrice(), p.getArea(),
						p.getPropertyType(), p.getTransactionType()));
			return defaultReadResponse(id);
	}

    //defaultRead works
        private staticxfile.txt Mono<ServerResponse> defaultReadResponse(Publisher<Profile> profiles) {
        return ServerResponse
            .ok()
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(profiles, Profile.class);
    }

    private staticxfile.txt String id(ServerRequest r) {
        return r.pathVariable("id");
    }


     */
}
