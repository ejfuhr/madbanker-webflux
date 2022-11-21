package com.reactive.banker.service;

import com.reactive.banker.entity.Contract;
import org.springframework.context.ApplicationEvent;

public class ContractCreatedEvent extends ApplicationEvent {


    public ContractCreatedEvent(Contract source) {
        super(source);
    }
}
