package com.chopikus.manager_app;

public class SupplierModel {

    String contract="", contractNumber="", phoneNumber="", id="", contractLink="", name="";

    public SupplierModel(String name, String contractNumber, String phoneNumber, String id, String contractLink) {
        this.name = name;
        this.contractNumber = contractNumber;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.contractLink = contractLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContractLink() {
        return contractLink;
    }

    public void setContractLink(String contractLink) {
        this.contractLink = contractLink;
    }

}