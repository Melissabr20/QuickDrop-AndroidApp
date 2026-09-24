package com.example.QuickDrop.core.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArticleAvailability {
    private List<ProduitArticle> produitArticle;

    public List<ProduitArticle> getProduitArticle() {
        return produitArticle;
    }

    public void setProduitArticle(List<ProduitArticle> produitArticle) {
        this.produitArticle = produitArticle;
    }

    public static class ProduitArticle {
        @SerializedName("id")
        private int id;

        @SerializedName("stock_disponible")
        private int stockDisponible;

        @SerializedName("article")
        private int articleId;

        @SerializedName("fournisseur")
        private int fournisseurId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getStockDisponible() {
            return stockDisponible;
        }

        public void setStockDisponible(int stockDisponible) {
            this.stockDisponible = stockDisponible;
        }

        public int getArticleId() {
            return articleId;
        }

        public void setArticleId(int articleId) {
            this.articleId = articleId;
        }

        public int getFournisseurId() {
            return fournisseurId;
        }

        public void setFournisseurId(int fournisseurId) {
            this.fournisseurId = fournisseurId;
        }
    }
}

