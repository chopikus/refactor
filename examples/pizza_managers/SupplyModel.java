package com.chopikus.manager_app;

import org.json.JSONArray;

public class SupplyModel {

    String shipperName="",shipperId="",cafeName="",cafeId="", name="", JSONSum="";
    JSONArray supplies;
    String contractNumber="";

    public SupplyModel(String shipperName, String shipperId, String cafeName, String cafeId, String name, JSONArray supplies) {
        this.shipperName = shipperName;
        this.shipperId = shipperId;
        this.cafeName = cafeName;
        this.cafeId = cafeId;
        this.name = name;
        this.supplies = supplies;
    }

    public String getShipperName() {
        return shipperName;
    }

    public String getShipperId() {
        return shipperId;
    }

    public String getCafeName() {
        return cafeName;
    }

    public String getCafeId() {
        return cafeId;
    }

    public String getName() {
        return name;
    }

    public JSONArray getSupplies() {
        return supplies;
    }
    public int getCount()
    {
        return supplies.length();
    }
    public String getContractNumber() {
        return contractNumber;
    }
}