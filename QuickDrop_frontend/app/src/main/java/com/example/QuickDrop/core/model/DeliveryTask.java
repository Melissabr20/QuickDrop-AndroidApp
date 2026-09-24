package com.example.QuickDrop.core.model;
public class DeliveryTask {
    private String name,time,id_ord,adr_four,adr_client;
    private int prix_livraison;

    public int getPrix_livraison() {
        return prix_livraison;
    }

    public void setPrix_livraison(int prix_livraison) {
        this.prix_livraison = prix_livraison;
    }

    public DeliveryTask() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId_ord() {
        return id_ord;
    }

    public void setId_ord(String id_ord) {
        this.id_ord = id_ord;
    }

    public String getAdr_four() {
        return adr_four;
    }

    public void setAdr_four(String adr_four) {
        this.adr_four = adr_four;
    }

    public String getAdr_client() {
        return adr_client;
    }

    public void setAdr_client(String adr_client) {
        this.adr_client = adr_client;
    }


}
