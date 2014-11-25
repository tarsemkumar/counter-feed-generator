package com.nature.feeds.startup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.inject.Injector;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.service.BookAndCollectionMemberFeedDataService;
import com.nature.feeds.service.CollectionFeedDataService;
import com.nature.feeds.service.EmailNotificationService;
import com.nature.feeds.service.GenerateBookFeedService;
import com.nature.feeds.service.GenerateCollectionFeedService;
import com.nature.feeds.service.GenerateCollectionMemberFeedService;
import com.nature.feeds.service.UploadFeedService;
import com.util.Constants;

public class FeedGenerator {

    CollectionFeedDataService collectionFeedDataService;
    GenerateCollectionFeedService generateCollectionFeedService;
    BookAndCollectionMemberFeedDataService bookAndCollectionMemberFeedDataService;
    List<CollectionBean> collectionFeedDataList = new ArrayList<CollectionBean>();
    ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources");
    Injector injector = null;
    ResultsBean resultsBean = new ResultsBean();
    GenerateBookFeedService generateBookFeedService = null;
    GenerateCollectionMemberFeedService generateCollectionMemberFeedService = null;
    UploadFeedService uploadFeedService = null;
    EmailNotificationService emailNotificationService = null;

    public FeedGenerator(Injector injector) {
        this.injector = injector;
        this.collectionFeedDataService = injector.getInstance(CollectionFeedDataService.class);
        this.generateCollectionFeedService = injector.getInstance(GenerateCollectionFeedService.class);
        this.bookAndCollectionMemberFeedDataService = injector
                .getInstance(BookAndCollectionMemberFeedDataService.class);
        this.generateBookFeedService = injector.getInstance(GenerateBookFeedService.class);
        this.generateCollectionMemberFeedService = injector.getInstance(GenerateCollectionMemberFeedService.class);
        this.uploadFeedService = injector.getInstance(UploadFeedService.class);
        this.emailNotificationService = injector.getInstance(EmailNotificationService.class);
    }

    /* This method will use to get data for all three feeds */

    public void fetchFeedData() {

        Constants.INFO.info("**** Feed generation application has been started. ****");
        try {
            Constants.INFO.info("\n**** Phase 1:- Fetching feed data has been started. ****");
            collectionFeedDataList = collectionFeedDataService.getCollectionFeedData();
            resultsBean = bookAndCollectionMemberFeedDataService.getBookAndCollectionMemberFeedData(injector);
            Constants.INFO.info("\n**** Phase 1:- Fetching feed data has been completed. ****");
            generateFeed();
        } catch (Exception e) {
            Constants.ERROR.error("Exception during fetching feed data.\n" + e);
            emailNotification(messages.getString("feed.generation.failure.mail.to"),
                    messages.getString("feed.generation.failure.mail.subject"),
                    messages.getString("feed.generation.failure.mail.body"));
        }
    }

    /* This method will use to generate all three feeds */

    private void generateFeed() {
        try {
            Constants.INFO.info("\n**** Phase 2:- Feed generation has been started. ****");
            String currentDateInYYYYMMDDFormat = getDateInYYYYMMDDFormat();
            String collectionFeedName = messages.getString("collection") + currentDateInYYYYMMDDFormat
                    + messages.getString("excel.extension");
            generateCollectionFeedService.generateCollectionFeed(collectionFeedName,
                    messages.getString("file.location"), collectionFeedDataList);
            String bookFeedName = messages.getString("book") + currentDateInYYYYMMDDFormat
                    + messages.getString("excel.extension");
            Map<String, String> groupCodes = generateBookFeedService.getCollectionGroupMap();
            generateBookFeedService.generateMpsBookFeed(bookFeedName, messages.getString("file.location"), resultsBean,
                    groupCodes);
            String collectionMemberFeedName = messages.getString("collection.member") + currentDateInYYYYMMDDFormat
                    + messages.getString("excel.extension");
            generateCollectionMemberFeedService.generateCollctionMemberFeed(collectionMemberFeedName,
                    messages.getString("file.location"), resultsBean);

            Constants.INFO.info("\n**** Phase 2:- Feed generation has been completed. ****");
            uploadFeed(getTodaysFeedNamesList(collectionFeedName, bookFeedName, collectionMemberFeedName));
        } catch (Exception e) {
            Constants.ERROR.error("Exception during generating feeds.\n" + e);
            emailNotification(messages.getString("feed.generation.failure.mail.to"),
                    messages.getString("feed.generation.failure.mail.subject"),
                    messages.getString("feed.generation.failure.mail.body"));
        }
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
        yesterdaysFeedNameList.add(messages.getString("collection") + yesterdayDateInYYYYMMDDFormat
                + messages.getString("excel.extension"));
        yesterdaysFeedNameList.add(messages.getString("book") + yesterdayDateInYYYYMMDDFormat
                + messages.getString("excel.extension"));
        yesterdaysFeedNameList.add(messages.getString("collection.member") + yesterdayDateInYYYYMMDDFormat
                + messages.getString("excel.extension"));
        yesterdaysFeedNameList.trimToSize();
        return yesterdaysFeedNameList;
    }

    /* This method will use to upload all three feeds on FTP location */

    private void uploadFeed(ArrayList<String> todaysFeedName) {
        try {
            Constants.INFO.info("\n**** Phase 3:- Feed uploading has been started. ****");
            Boolean feedUploadStatus = Boolean.FALSE;
            feedUploadStatus = uploadFeedService.uploadFeedsOnFTPLocation(todaysFeedName, getYesterdaysFeedNameList());
            if (feedUploadStatus == Boolean.FALSE) {
                emailNotification(messages.getString("feed.generation.failure.mail.to"),
                        messages.getString("feed.generation.failure.mail.subject"),
                        messages.getString("feed.generation.failure.mail.body"));
            } else {
                Constants.INFO.info("\n**** Phase 3:- Feed uploading has been completed. ****");
                emailNotification(messages.getString("mail.to"), messages.getString("mail.subject"),
                        messages.getString("mail.body"));
            }

        } catch (Exception e) {
            Constants.ERROR.error("Exception during uploading feeds.\n" + e);
            emailNotification(messages.getString("feed.generation.failure.mail.to"),
                    messages.getString("feed.generation.failure.mail.subject"),
                    messages.getString("feed.generation.failure.mail.body"));
        }
    }

    /* This method will use to send email notification */

    private void emailNotification(String to, String subject, String body) {
        try {
            Constants.INFO.info("\n**** Phase 4:- E-mail notification has been started. ****");
            emailNotificationService.sendEmailNotification(to, subject, body);
            Constants.INFO.info("\n**** Phase 4:- E-mail notification has been completed. ****");
            Constants.INFO.info("\n**** Feed generation application has been completed. ****\n\n\n");

        } catch (Exception e) {
            Constants.ERROR.error("Exception during sending email notification.\n" + e);
        }
    }

    private String getDateInYYYYMMDDFormat() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(messages.getString("date.format"));
        return sdf.format(Calendar.getInstance().getTime());
    }

    private String getYesterdayDateInYYYYMMDDFormat() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(messages.getString("date.format"));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return sdf.format(cal.getTime());
    }
}
