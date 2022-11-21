package com.reactive.banker.service;


import com.reactive.banker.entity.MadBanker;
import org.springframework.context.ApplicationEvent;

public class BankerCreatedEvent extends ApplicationEvent {


    public BankerCreatedEvent(MadBanker source) {
        super(source);
    }
}
