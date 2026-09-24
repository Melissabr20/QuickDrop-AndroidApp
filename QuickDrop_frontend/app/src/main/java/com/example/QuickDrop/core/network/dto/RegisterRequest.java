package com.example.QuickDrop.core.network.dto;
public class RegisterRequest {
    private String email;

    private String username;
    private String password;
    private String phone_number;
    private String type;
    private String adress;

    private String adminetat;

    public void setEtat(String etat) {
        this.adminetat = etat;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
