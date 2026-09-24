package com.example.QuickDrop.core.model;

import com.example.QuickDrop.core.network.dto.LoginResponse;
public class DeliveryAddress {
    private int id;
    private String city;
    private String wilaya;
    private String address;
    private String lat; 
    private String lng;
    private String place_id;
    private LoginResponse.User user;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWilaya() {
        return wilaya;
    }

    public void setWilaya(String wilaya) {
        this.wilaya = wilaya;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public LoginResponse.User getUser() {
        return user;
    }

    public void setUser(LoginResponse.User user) {
        this.user = user;
    }

 

}




