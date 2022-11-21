package com.reactive.banker.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection="madBanker")
public class MadBanker implements Serializable {

	
	@Id
	private String id;
	private String bankerId;
	private String lastName;
	private String nickName;
	private String mailCode;
	
	//@DocumentReference(lazy = true)
	private List<Contract> contracts;
	public MadBanker() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MadBanker(String bankerId) {
		super();
		this.bankerId = bankerId;
	}

	public MadBanker(String id, String bankerId, String lastName, String nickName, String mailCode) {
		this.id = id;
		this.bankerId = bankerId;
		this.lastName = lastName;
		this.nickName = nickName;
		this.mailCode = mailCode;
	}

	public MadBanker(String id, String bankerId, String lastName, String nickName, String mailCode, List<Contract> contracts) {
		this.id = id;
		this.bankerId = bankerId;
		this.lastName = lastName;
		this.nickName = nickName;
		this.mailCode = mailCode;
		this.contracts = contracts;
	}

	public MadBanker(String bankerId, String lastName, String nickName, String mailCode) {
		this.bankerId = bankerId;
		this.lastName = lastName;
		this.nickName = nickName;
		this.mailCode = mailCode;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBankerId() {
		return bankerId;
	}
	public void setBankerId(String bankerId) {
		this.bankerId = bankerId;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
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
		return "MadBanker [id=" + id + ", bankerId=" + bankerId + ", lastName=" + lastName + ", nickName=" + nickName
				+ ", mailCode=" + mailCode + ", contracts=" + contracts + "]";
	}
	
	
}
