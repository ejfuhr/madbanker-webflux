package com.reactive.banker.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "banking_aggregation")
public class BankingAgg {
    @Id
    private String id;

    private String aggregateId = "defaultId-001";
    private String addNote;
    //@DocumentReference(lazy = true)
    private Client client;
    //@DocumentReference(lazy = true)
    private Contract contract;
    //@DocumentReference(lazy = true)
    private MadBanker madBanker;
    //@DocumentReference(lazy = true)
    private Property property;

    //@DocumentReference(lazy = true)
    private List<Client> clientList;
    //@DocumentReference(lazy = true)
    private List<Contract> contractList;
    //@DocumentReference(lazy = true)
    private List<MadBanker> bankerList;
    //@DocumentReference(lazy = true)
    private List<Property> propertyList;

    //@PersistenceCreator
    public BankingAgg(){

    }

    /**
     * All data from Lists starting with the aggregateId
     * @param aggregateId
     * @param clientList
     * @param contractList
     * @param bankerList
     * @param propertyList
     */
    public BankingAgg(String aggregateId, List<Client> clientList, List<Contract> contractList,
                      List<MadBanker> bankerList, List<Property> propertyList) {
        this.aggregateId = aggregateId;
        this.clientList = clientList;
        this.contractList = contractList;
        this.bankerList = bankerList;
        this.propertyList = propertyList;
    }

    /**
     * Use to initiate single Client with a contractList and a propertyList
     * @param aggregateId
     * @param client
     * @param contractList
     * @param propertyList
     */

    public BankingAgg(String aggregateId, Client client, List<Contract> contractList, List<Property> propertyList) {
        this.aggregateId = aggregateId;
        this.client = client;
        this.contractList = contractList;
        this.propertyList = propertyList;
    }

    public BankingAgg(String aggregateId, Client client, List<Contract> contractList, List<Property> propertyList,
                      List<MadBanker> bankerList
                      ) {
        this.aggregateId = aggregateId;
        this.client = client;
        this.contractList = contractList;
        this.propertyList = propertyList;
        this.bankerList = bankerList;
    }
    /*
    Mono<String> aggId,
                                                                            Mono<Client> clientMono,
                                                                            Mono<List<Contract>> contractMonoList,
                                                                            Mono<List<MadBanker>> bankerMonoList,
                                                                            Mono<List<Property>> propertyMonoList)
     */

    public BankingAgg(String aggregateId, Client client, Contract contract, MadBanker madBanker, Property property) {
        this.aggregateId = aggregateId;
        this.client = client;
        this.contract = contract;
        this.madBanker = madBanker;
        this.property = property;
    }

    public BankingAgg(String aggregateId, Client client, Contract contract, MadBanker madBanker) {
        this.aggregateId = aggregateId;
        this.client = client;
        this.contract = contract;
        this.madBanker = madBanker;
    }

    public BankingAgg(String aggregateId, Client client, List<Contract> contractList, List<Property> propertyList,
                      MadBanker madBanker) {
        this.aggregateId = aggregateId;
        this.client = client;
        this.madBanker = madBanker;
        this.contractList = contractList;
        this.propertyList = propertyList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddNote() {
        return addNote;
    }

    public void setAddNote(String addNote) {
        this.addNote = addNote;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public MadBanker getMadBanker() {
        return madBanker;
    }

    public void setMadBanker(MadBanker madBanker) {
        this.madBanker = madBanker;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public List<Client> getClientList() {
        if(null == clientList){
            this.clientList = new ArrayList<Client>();
        }
        return clientList;
    }

    public void setClientList(List<Client> clientList) {
        this.clientList = clientList;
    }

    public List<Contract> getContractList() {
        if(null == contractList){
            this.contractList = new ArrayList<Contract>();
        }
        return contractList;
    }

    public void setContractList(List<Contract> contractList) {
        this.contractList = contractList;
    }

    public List<MadBanker> getBankerList() {
        if(null == bankerList){
            this.bankerList = new ArrayList<MadBanker>();
        }
        return bankerList;
    }

    public void setBankerList(List<MadBanker> bankerList) {
        this.bankerList = bankerList;
    }

    public List<Property> getPropertyList() {
        if(null == propertyList){
            this.propertyList = new ArrayList<Property>();
        }
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    @Override
    public String toString() {
        return "BankingAgg{" +
                "id='" + id + '\'' +
                ", aggregateId='" + aggregateId + '\'' +
                '}';
    }
}
