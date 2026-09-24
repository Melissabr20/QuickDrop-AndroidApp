package com.example.QuickDrop.core.network.dto;

import com.example.QuickDrop.core.model.DeliveryOrderLine;
import java.util.List;

public class GetPanierResponse {
    private int id;
    private int client;
    Double montant;
    private int livreur;
    private String status ;

    public int getAdresse_livraison() {
        return adresse_livraison;
    }

    public void setAdresse_livraison(int adresse_livraison) {
        this.adresse_livraison = adresse_livraison;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private int adresse_livraison;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    public List<DeliveryOrderLine> getLignes_livraison() {
        return lignes_livraison;
    }

    public void setLignes_livraison(List<DeliveryOrderLine> lignes_livraison) {
        this.lignes_livraison = lignes_livraison;
    }

    private int prix_livraison;
    private int fournisseur ;
    private Boolean valide;
    private String date;

    public String getDate() {
        return date;
    }

    public int getPrix_livraison() {
        return prix_livraison;
    }

    public void setPrix_livraison(int prix_livraison) {
        this.prix_livraison = prix_livraison;
    }



    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    private List<DeliveryOrderLine> lignes_livraison; // Correction du nom du champ

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    // Correction du nom de la méthode getter
    public List<DeliveryOrderLine> getLigneLivraison() {
        return lignes_livraison;
    }

    public void setLigneLivraison(List<DeliveryOrderLine> ligneLivraison) {
        this.lignes_livraison = ligneLivraison;
    }


}

