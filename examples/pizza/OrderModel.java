package com.chopikus.bizzarepizzaoperator;

import java.util.ArrayList;

public class OrderModel {

    String name;
    int id_;
    String address;
    String phone_number;
    String status;
    ArrayList<String> printList;
    public OrderModel(String name, int id_, String address, String phone_number, String status, ArrayList<String> printList) {
        this.name = name;
        this.id_ = id_;
        this.address = address;
        this.phone_number = phone_number;
        this.phone_number = this.phone_number.replace(' ', '+');
        this.status = status;
        this.printList = printList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getPrintList() {
        return printList;
    }

    public void setPrintList(ArrayList<String> printList) {
        this.printList = printList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId_() {
        return id_;
    }

    public void setId_(int id_) {
        this.id_ = id_;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }
}