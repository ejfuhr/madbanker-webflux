package com.reactive.banker.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;


@Document(collection = "client")
public class Client implements Serializable {
	
	@Id
	private String id;
	private String clientId;
	private String firstName;
	private String lastName;
	private String businessAddress;
	private String city;
	private String state;
	private String mailCode;
	
	//@DocumentReference(lazy = true)
	private List<Contract> contracts;
	public Client() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Client(String clientId) {
		super();
		this.clientId = clientId;
	}

	public Client(String clientId, String firstName, String lastName, String businessAddress, String city, String state,
				  String mailCode) {
		this.clientId = clientId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.businessAddress = businessAddress;
		this.city = city;
		this.state = state;
		this.mailCode = mailCode;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getBusinessAddress() {
		return businessAddress;
	}
	public void setBusinessAddress(String businessAddress) {
		this.businessAddress = businessAddress;
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
	public List<Contract> getContracts() {
		if(null == contracts) {
			contracts = new ArrayList<Contract>();
		}
		return contracts;
	}
	public void setContracts(List<Contract> contracts) {
		this.contracts = contracts;
	}
	@Override
	public String toString() {
		return "Client [id=" + id + ", clientId=" + clientId + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", businessAddress=" + businessAddress + ", city=" + city + ", state=" + state + ", mailCode="
				+ mailCode + ", contracts=" + contracts + "]";
	}
	
	
	

}
