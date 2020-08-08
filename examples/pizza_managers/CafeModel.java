package com.chopikus.manager_app;

import android.graphics.Bitmap;

public class CafeModel {

    String name;
    int id_;
    Bitmap image;
    String address;
    public CafeModel(String name, String address, int id_, Bitmap image) {
        this.name = name;
        this.address = address;
        this.id_ = id_;
        this.image=image;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Bitmap getImage() {
        return image;
    }

    public int getId() {
        return id_;
    }
}