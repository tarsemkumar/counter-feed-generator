package com.nature.feeds.service;

import com.google.inject.Injector;
import com.nature.feeds.bean.ResultsBean;

public interface BookAndCollectionMemberFeedDataService {

    public ResultsBean getBookAndCollectionMemberFeedData(Injector injector) throws Exception;

}
