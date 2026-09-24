package com.example.QuickDrop.core.network.dto;
public class GetFournisseurResponse {
    private int id;
    private String name;
    private String city;
    private String country;
    private String adress;
    private Double lat;
    private Boolean is_fourniseur;
    private LoginResponse.User user;

    public Boolean getIs_fourniseur() {
        return is_fourniseur;
    }

    public void setIs_fourniseur(Boolean is_fourniseur) {
        this.is_fourniseur = is_fourniseur;
    }

    public int getUser() {
        return user.getId();
    }

    public void setUser( LoginResponse.User user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    private Double lng;
    private String place_id;



}
