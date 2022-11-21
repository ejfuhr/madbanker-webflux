package com.reactive.banker.controller;

import com.reactive.banker.entity.Client;
import com.reactive.banker.entity.MadBanker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.ServerRequestWrapper;
import reactor.core.publisher.Mono;


import java.net.URI;
import java.util.Map;
@Slf4j
public class BankerRequestPredicate implements RequestPredicate {

    private RequestPredicate target;

    public BankerRequestPredicate(RequestPredicate target) {
        this.target = target;
    }

    @Override
    public boolean test(ServerRequest request) {

        LowerCaseUriServerRequestWrapper low = new LowerCaseUriServerRequestWrapper(request);

        //Mono<MadBanker> book = request.body(BodyExtractors.toMono(MadBanker.class));
        low.printVars();
        printStuff(request);
        System.out.println("URI PATH ::" + request.uri().getRawPath());


        //return this.target.test(low);
        return true;
    }

    @Override
    public String toString() {
        return this.target.toString();
    }

    private void printStuff(ServerRequest request){

        log.debug("exchange,request {} , pathVaribales notNull {} ",
                request.exchange().getRequest().toString(),
                request.pathVariables() != null);
        if(request.pathVariables() != null) {
            printVars(request.pathVariables());
        }

    }

    private void printVars(Map<String, String> varMap){
        System.out.println("PRINTING MAP with SIZE " + varMap.size());

        varMap.forEach((k, v) -> {
            System.out.println("KEY:: " + k + " VAL:: " + v);
        });
    }
}

class LowerCaseUriServerRequestWrapper extends ServerRequestWrapper {

    LowerCaseUriServerRequestWrapper(ServerRequest delegate) {
        super(delegate);
    }



    public void printVars(){
        Map<String, String> varMap= this.pathVariables();
        varMap.forEach((k, v) -> {
            System.out.println("KEY:: " + k + " VAL:: " + v);
        });

        //return this.bodyToMono(Client.class);
    }

    @Override
    public URI uri() {
        return URI.create(super.uri().toString().toLowerCase());
    }

    @Override
    public String path() {
        return uri().getRawPath();
    }

    @Override
    public PathContainer pathContainer() {
        return PathContainer.parsePath(path());
    }

    @Override
    public RequestPath requestPath(){
        return RequestPath.parse(uri(), path()

        );
    }
}