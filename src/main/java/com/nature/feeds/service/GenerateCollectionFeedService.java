package com.nature.feeds.service;

import java.util.List;

import com.nature.feeds.bean.CollectionBean;

public interface GenerateCollectionFeedService {

    public void generateCollectionFeed(String feedName, String feedPath, List<CollectionBean> list) throws Exception;

}
