package com.nature.feeds.bean;

import java.util.ArrayList;
import java.util.List;

public class ItemBean {

    public ItemBean() {
        collections = new ArrayList<CollectionBean>();
    }

    private String thirteenDigitIsbn;
    private String doi;
    private String title;
    private final List<CollectionBean> collections;

    public String getThirteenDigitIsbn() {
        return thirteenDigitIsbn;
    }

    public void setThirteenDigitIsbn(String thirteenDigitIsbn) {
        this.thirteenDigitIsbn = thirteenDigitIsbn;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addCollection(CollectionBean collectionBean) {
        collections.add(collectionBean);
    }

    public List<CollectionBean> getCollections() {
        return collections;
    }
}
