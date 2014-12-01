package com.nature.feeds.startup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.service.BookAndCollectionMemberFeedDataService;
import com.nature.feeds.service.CollectionFeedDataService;
import com.nature.feeds.service.EmailNotificationService;
import com.nature.feeds.service.GenerateBookFeedService;
import com.nature.feeds.service.GenerateCollectionFeedService;
import com.nature.feeds.service.GenerateCollectionMemberFeedService;
import com.nature.feeds.service.UploadFeedService;

public class FeedGenerator {

    private final CollectionFeedDataService collectionFeedDataService;
    private final GenerateCollectionFeedService generateCollectionFeedService;
    private final BookAndCollectionMemberFeedDataService bookAndCollectionMemberFeedDataService;
    private List<CollectionBean> collectionFeedDataList;
    private ResultsBean resultsBean;
    private final GenerateBookFeedService generateBookFeedService;
    private final GenerateCollectionMemberFeedService generateCollectionMemberFeedService;
    private final UploadFeedService uploadFeedService;
    private final EmailNotificationService emailNotificationService;
    private final IResourceLookUp resourceLookUp;
    private static Logger logger = Logger.getLogger(FeedGenerator.class);

    @Inject
    public FeedGenerator(CollectionFeedDataService collectionFeedDataService,
            GenerateCollectionFeedService generateCollectionFeedService,
            BookAndCollectionMemberFeedDataService bookAndCollectionMemberFeedDataService,
            GenerateBookFeedService generateBookFeedService,
            GenerateCollectionMemberFeedService generateCollectionMemberFeedService,
            UploadFeedService uploadFeedService, EmailNotificationService emailNotificationService,
            @Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.collectionFeedDataService = collectionFeedDataService;
        this.generateCollectionFeedService = generateCollectionFeedService;
        this.bookAndCollectionMemberFeedDataService = bookAndCollectionMemberFeedDataService;
        this.generateBookFeedService = generateBookFeedService;
        this.generateCollectionMemberFeedService = generateCollectionMemberFeedService;
        this.uploadFeedService = uploadFeedService;
        this.emailNotificationService = emailNotificationService;
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to get data for all three feeds */

    public void fetchFeedData() {

        logger.info("**** Feed generation application has been started. ****");
        try {
            logger.info("**** Phase 1:- Fetching feed data has been started. ****");
            collectionFeedDataList = collectionFeedDataService.getCollectionFeedData();
            resultsBean = bookAndCollectionMemberFeedDataService.getBookAndCollectionMemberFeedData();
            logger.info("**** Phase 1:- Fetching feed data has been completed. ****");
            generateFeed();
        } catch (Exception exception) {
            logger.info(exception);
            try {
                emailNotificationWhenFeedsDonotGenerate();
            } catch (Exception exception_email) {
                logger.info(exception_email);
            }
        }
    }

    /* This method will use to generate all three feeds */

    private void generateFeed() throws Exception {
        logger.info("**** Phase 2:- Feed generation has been started. ****");
        String currentDateInYYYYMMDDFormat = getDateInYYYYMMDDFormat();
        String collectionFeedName = resourceLookUp.getResource("collection") + currentDateInYYYYMMDDFormat
                + resourceLookUp.getResource("excel.extension");
        generateCollectionFeedService.generateCollectionFeed(collectionFeedName,
                resourceLookUp.getResource("file.location"), collectionFeedDataList);
        String bookFeedName = resourceLookUp.getResource("book") + currentDateInYYYYMMDDFormat
                + resourceLookUp.getResource("excel.extension");
        Map<String, String> groupCodes = generateBookFeedService.getCollectionGroupMap();
        generateBookFeedService.generateMpsBookFeed(bookFeedName, resourceLookUp.getResource("file.location"),
                resultsBean, groupCodes);
        String collectionMemberFeedName = resourceLookUp.getResource("collection.member") + currentDateInYYYYMMDDFormat
                + resourceLookUp.getResource("excel.extension");
        generateCollectionMemberFeedService.generateCollctionMemberFeed(collectionMemberFeedName,
                resourceLookUp.getResource("file.location"), resultsBean);

        logger.info("**** Phase 2:- Feed generation has been completed. ****");
        uploadFeed(getTodaysFeedNamesList(collectionFeedName, bookFeedName, collectionMemberFeedName));
    }

    /* This method will use to get todaysFeedNameList */

    private ArrayList<String> getTodaysFeedNamesList(String collectionFeedName, String bookFeedName,
            String collectionMemberFeedName) throws Exception {
        ArrayList<String> todaysFeedNameList = new ArrayList<String>();
        todaysFeedNameList.add(collectionFeedName);
        todaysFeedNameList.add(bookFeedName);
        todaysFeedNameList.add(collectionMemberFeedName);
        todaysFeedNameList.trimToSize();
        return todaysFeedNameList;

    }

    /* This method will use to get yesterdaysFeedNameList */

    private ArrayList<String> getYesterdaysFeedNameList() throws Exception {
        String yesterdayDateInYYYYMMDDFormat = getYesterdayDateInYYYYMMDDFormat();
        ArrayList<String> yesterdaysFeedNameList = new ArrayList<String>();
        yesterdaysFeedNameList.add(resourceLookUp.getResource("collection") + yesterdayDateInYYYYMMDDFormat
                + resourceLookUp.getResource("excel.extension"));
        yesterdaysFeedNameList.add(resourceLookUp.getResource("book") + yesterdayDateInYYYYMMDDFormat
                + resourceLookUp.getResource("excel.extension"));
        yesterdaysFeedNameList.add(resourceLookUp.getResource("collection.member") + yesterdayDateInYYYYMMDDFormat
                + resourceLookUp.getResource("excel.extension"));
        yesterdaysFeedNameList.trimToSize();
        return yesterdaysFeedNameList;
    }

    /* This method will use to upload all three feeds on FTP location */

    private void uploadFeed(ArrayList<String> todaysFeedName) throws Exception {
        logger.info("**** Phase 3:- Feed uploading has been started. ****");
        Boolean feedUploadStatus = Boolean.FALSE;
        feedUploadStatus = uploadFeedService.feedsUploadOperation(todaysFeedName, getYesterdaysFeedNameList());
        if (feedUploadStatus == Boolean.FALSE) {
            emailNotificationWhenFeedsDonotGenerate();
        } else {
            logger.info("**** Phase 3:- Feed uploading has been completed. ****");
            emailNotificationWhenFeedsGenerate();
        }
    }

    /* This method will use to send email notification when feeds generate successfully. */

    private void emailNotificationWhenFeedsGenerate() throws Exception {
        logger.info("**** Phase 4:- E-mail notification has been started. ****");
        emailNotificationService.sendEmailNotification(resourceLookUp.getResource("mail.to"),
                resourceLookUp.getResource("mail.subject"), resourceLookUp.getResource("mail.body"));
        logger.info("**** Phase 4:- E-mail notification has been completed. ****");
        logger.info("**** Feed generation application has been completed. ****\n\n\n");
    }

    /* This method will use to send email notification when feeds does not generate successfully. */

    private void emailNotificationWhenFeedsDonotGenerate() throws Exception {
        logger.info("**** Phase 4:- E-mail notification has been started. ****");
        emailNotificationService.sendEmailNotification(resourceLookUp.getResource("feed.generation.failure.mail.to"),
                resourceLookUp.getResource("feed.generation.failure.mail.subject"),
                resourceLookUp.getResource("feed.generation.failure.mail.body"));
        logger.info("**** Phase 4:- E-mail notification has been completed. ****");
        logger.info("**** Feed generation application has been completed. ****\n\n\n");
    }

    private String getDateInYYYYMMDDFormat() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(resourceLookUp.getResource("date.format"));
        return sdf.format(Calendar.getInstance().getTime());
    }

    private String getYesterdayDateInYYYYMMDDFormat() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(resourceLookUp.getResource("date.format"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return sdf.format(cal.getTime());
    }
}
