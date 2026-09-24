package com.example.QuickDrop.core.network.dto;
import java.util.Date;

public class GetOrderResponse {
    private int id;
    private float montant;
    private int client;
    private int livreur ;
    private int fournisseur;
    private String status;
    private Date date;
    private boolean valide;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getMontant() {
        return montant;
    }

    public void setMontant(float montant) {
        this.montant = montant;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public int getLivreur() {
        return livreur;
    }

    public void setLivreur(int livreur) {
        this.livreur = livreur;
    }

    public int getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(int fournisseur) {
        this.fournisseur = fournisseur;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
