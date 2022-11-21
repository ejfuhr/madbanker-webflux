package com.reactive.banker.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection="contract")
public class Contract implements Serializable {

	@Id
	private String id;
	private String contractId;
	private Double originalAmount;
	private Double amountDue;
	private Double interestPaid;
	// @DocumentReference(lazy = true)
	private List<Client> clients;

	//@DocumentReference(lazy = true)
	private List<Property> properties;
	
	//@DocumentReference(lazy = true)
	private List<MadBanker> bankers;
	public Contract() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Contract(String contractId) {
		super();
		this.contractId = contractId;
	}

	public Contract(String contractId, Double originalAmount, Double amountDue, Double interestPaid) {
		this.contractId = contractId;
		this.originalAmount = originalAmount;
		this.amountDue = amountDue;
		this.interestPaid = interestPaid;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getContractId() {
		return contractId;
	}
	public void setContractId(String contractId) {
		this.contractId = contractId;
	}
	public Double getOriginalAmount() {
		return originalAmount;
	}
	public void setOriginalAmount(Double originalAmount) {
		this.originalAmount = originalAmount;
	}
	public Double getAmountDue() {
		return amountDue;
	}
	public void setAmountDue(Double amountDue) {
		this.amountDue = amountDue;
	}
	public Double getInterestPaid() {
		return interestPaid;
	}
	public void setInterestPaid(Double interestPaid) {
		this.interestPaid = interestPaid;
	}

	public List<Client> getClients() {
		if(null == clients){
			this.clients = new ArrayList<Client>();
		}
		return clients;
	}

	public void setClients(List<Client> clients) {
		this.clients = clients;
	}

	public List<Property> getProperties() {
		if(null == properties) {
			properties = new ArrayList<Property>();
		}
		return properties;
	}
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
	public List<MadBanker> getBankers() {
		if(null == bankers) {
			bankers = new ArrayList<MadBanker>();
		}
		return bankers;
	}
	public void setBankers(List<MadBanker> bankers) {
		this.bankers = bankers;
	}
	@Override
	public String toString() {
		return "Contract [id=" + id + ", contractId=" + contractId + ", originalAmount=" + originalAmount
				+ ", amountDue=" + amountDue + ", interestPaid=" + interestPaid + "]";
	}
	
	
	
}
