package com.nature.feeds.service;

import java.util.Map;

import com.nature.feeds.bean.ResultsBean;

public interface GenerateBookFeedService {

    public void generateMpsBookFeed(String fileName, String filePath, ResultsBean feedDataBeans,
            Map<String, String> groupCodes) throws Exception;

    public Map<String, String> getCollectionGroupMap() throws Exception;

}
