package com.example.QuickDrop.core.network.dto;
import java.util.List;

public class ConfirmResponse {
    private int id;
    private List<Integer> article;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getArticle() {
        return article;
    }

    public void setArticle(List<Integer> article) {
        this.article = article;
    }




}

