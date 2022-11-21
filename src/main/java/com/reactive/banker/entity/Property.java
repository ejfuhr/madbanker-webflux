package com.reactive.banker.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "property")	
public class Property implements Serializable {

	@Id	
	private String id;
	

	//@DocumentReference(lookup = "'propertyId' : ?#{#target} ")
	private String propertyId;
	private String streetAddress;
	private String city;
	private String state;
	private String mailCode;
	
	
	public Property(String id, String propertyId, String streetAddress, String city, 
			String state, String mailCode) {
		super();
		this.id = id;
		this.propertyId = propertyId;
		this.streetAddress = streetAddress;
		this.city = city;
		this.state = state;
		this.mailCode = mailCode;
	}
	public Property() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Property(String propertyId) {
		super();
		this.propertyId = propertyId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPropertyId() {
		return propertyId;
	}
	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getMailCode() {
		return mailCode;
	}
	public void setMailCode(String mailCode) {
		this.mailCode = mailCode;
	}
	@Override
	public String toString() {
		return "Property [id=" + id + ", propertyId=" + propertyId + ", streetAddress=" + streetAddress + ", city="
				+ city + ", state=" + state + ", mailCode=" + mailCode + "]";
	}
	
	
}
