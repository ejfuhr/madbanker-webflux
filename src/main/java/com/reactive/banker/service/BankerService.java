package com.reactive.banker.service;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.Contract;
import com.reactive.banker.entity.MadBanker;
import com.reactive.banker.repository.ClientRepository;
import com.reactive.banker.repository.MadBankerRepository;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class BankerService {

    @Autowired
    private MadBankerRepository bankerRepository;

    public Flux<MadBanker> getAllBankers(){
        return bankerRepository.findAll();
    }

    public Mono<MadBanker> findBankerByBankerId(String bankerId){

        return bankerRepository.findByBankerId(bankerId);
    }
/* */
    public Flux<Contract> findContractsByBankerId(String bankerId){
        return bankerRepository.findContractsByBankerId(bankerId);
    }

    public Mono<MadBanker> deleteById(String id){
        return bankerRepository.findById(id)
                .flatMap(p -> this.bankerRepository.deleteById(p.getId()).thenReturn(p));
    }

    public Mono<MadBanker> deleteByBankerId(String bankerId){
        return bankerRepository.findByBankerId(bankerId)
                .flatMap(p -> this.bankerRepository.deleteByBankerId(p.getBankerId()).thenReturn(p));
    }

    public Mono<MadBanker> saveBanker(MadBanker madBanker){
        return bankerRepository.save(madBanker);
    }

    public Mono<MadBanker> createBanker(String bankerId,
                                        String lastName,
                                        String nickName,
                                        String mailCode){
        log.debug("creating BANKER... {} {} {} {} ", bankerId, lastName, nickName, mailCode);
        MadBanker mb = new MadBanker(bankerId, lastName, nickName, mailCode);
        return bankerRepository
                .save(mb);
    }

    public Mono<MadBanker> updateBankerByBankerId(String bankerId, MadBanker banker) {
        Mono<MadBanker> bankerMono = bankerRepository
                .findByBankerId(bankerId)
                .map(cl -> new MadBanker(banker.getBankerId(), banker.getLastName(), banker.getNickName(),
                        banker.getMailCode()))
                .flatMap(cl -> bankerRepository.save(cl));
        return bankerMono;
    }

    public Mono<MadBanker> updateBankerByBankerIdWithContractList(String bankerId, MadBanker banker){
        Mono<MadBanker> bankerMono = bankerRepository
                .findByBankerId(bankerId)
                .map(b -> new MadBanker(b.getId(), bankerId, banker.getLastName(), banker.getNickName(),
                        banker.getMailCode(), b.getContracts())
                )
                .flatMap(b -> {
                    b.setBankerId(bankerId);
                    b.setId(banker.getId());
                    b.setNickName(banker.getNickName());
                    b.setLastName(banker.getLastName());
                    b.setMailCode(banker.getMailCode());
                    b.getContracts().addAll(banker.getContracts());
                    return bankerRepository.save(b);
                });
        return  bankerMono;
    }

    public Mono<MadBanker> updateByBankerBody(String bankerId, MadBanker banker){
        Mono<MadBanker> bankerMono = bankerRepository
                .findByBankerId(bankerId)
                .map(b -> new MadBanker(b.getId()))
                .flatMap(b -> {
                    b.setBankerId(bankerId);
                    b.setId(banker.getId());
                    b.setLastName(banker.getLastName());
                    b.setNickName(banker.getNickName());
                    b.setMailCode(banker.getMailCode());
                    return bankerRepository.save(b);
                });

        return bankerMono;
    }

    /*
    .map(p -> new Profile(p.getId(), email))
            .flatMap(this.profileRepository::save);
     */

    public Flux<MadBanker> saveAllBankers(Iterable<MadBanker> madBankersList){
        return bankerRepository.saveAll(madBankersList);
    }
    public Mono<Void> deleteAll() {
        return this.bankerRepository.deleteAll();
    }

    public Mono<Long> countBankers() {
        return this.bankerRepository.count();
    }

    /**
     * create List<Mono<MadBanker>> then create Flux<MadBanker> with Flux.mergeSequential,
     * then return Mono<List<MadBanker>>
     * @param bankerIds
     * @return  Mono<List<MadBanker>>
     */
    public Mono<List<MadBanker>> findBankersWithBankerIdsArray(String[] bankerIds){

        List<String> idList = List.of(bankerIds);
        List<Mono<MadBanker>> monoArrayList = new ArrayList<>();
        for (String id : idList) {
            log.debug("iterators id:::{}", id);
            monoArrayList.add(this.findBankerByBankerId(id));
        }

        Flux<MadBanker> bankerFlux = Flux.mergeSequential(monoArrayList);
        return  bankerFlux.collectList();
    }

}
