package com.nature.feeds.service;

import java.util.ArrayList;

public interface UploadFeedService {

    public Boolean uploadFeedsOnFTPLocation(ArrayList<String> todaysFeedName, ArrayList<String> yesterdaysFeedNames)
            throws Exception;

}
