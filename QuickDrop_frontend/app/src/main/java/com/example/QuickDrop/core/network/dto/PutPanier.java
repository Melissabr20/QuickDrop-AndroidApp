package com.example.QuickDrop.core.network.dto;
public class PutPanier {

    private int livreur;
    private int prix_livraison;
    private int fournisseur ;
    private Boolean valide;
    private int adresse_livraison;

    public int getAdresse_livraison() {
        return adresse_livraison;
    }

    public void setAdresse_livraison(int adresse_livraison) {
        this.adresse_livraison = adresse_livraison;
    }

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
    }


    public int getPrix_livraison() {
        return prix_livraison;
    }

    public void setPrix_livraison(int prix_livraison) {
        this.prix_livraison = prix_livraison;
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


}
