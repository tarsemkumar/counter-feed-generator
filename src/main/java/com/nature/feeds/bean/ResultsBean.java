package com.nature.feeds.bean;

import java.util.ArrayList;
import java.util.List;

public class ResultsBean {

    private final List<ItemBean> items;

    public ResultsBean() {
        items = new ArrayList<ItemBean>();
    }

    public List<ItemBean> getItems() {
        return items;
    }

    public void addItem(ItemBean item) {
        items.add(item);
    }
}
