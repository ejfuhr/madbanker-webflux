package com.reactive.banker.service;

import com.reactive.banker.entity.Client;
import org.springframework.context.ApplicationEvent;

public class ClientCreatedEvent extends ApplicationEvent {

    public  ClientCreatedEvent(Client source) {
        super(source);
    }
}