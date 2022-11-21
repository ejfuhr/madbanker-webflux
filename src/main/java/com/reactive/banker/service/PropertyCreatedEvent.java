package com.reactive.banker.service;

import org.springframework.context.ApplicationEvent;

import com.reactive.banker.entity.Property;

public class PropertyCreatedEvent extends ApplicationEvent {

	public  PropertyCreatedEvent(Property source) {
		super(source);
	}
}


