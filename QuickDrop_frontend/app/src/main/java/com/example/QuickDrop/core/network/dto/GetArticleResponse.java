package com.example.QuickDrop.core.network.dto;
import com.google.gson.annotations.SerializedName;


public class GetArticleResponse {

        private int id;
        private String name;
        @SerializedName("disignation")
        private String designation;
        private String price;
        @SerializedName("image")
        private String productimage;

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

        public String getDesignation() {
            return designation;
        }

        public void setDesignation(String designation) {
            this.designation = designation;
        }

        public String getPrice() {
            return price;
        }

    public String getProductimage() {
        return productimage;
    }

    public void setProductimage(String productimage) {
        this.productimage = productimage;
    }

    public void setPrice(String price) {
            this.price = price;
        }

}

