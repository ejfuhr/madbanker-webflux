package com.reactive.banker.service;

import com.reactive.banker.entity.BankingAgg;
import org.springframework.context.ApplicationEvent;

public class SimplaAggCreatedEvent extends ApplicationEvent {
    public SimplaAggCreatedEvent(BankingAgg source) {
        super(source);
    }
}
