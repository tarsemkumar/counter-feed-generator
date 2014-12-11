package com.nature.feeds.startup;

import java.text.SimpleDateFormat;
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
    private ResultsBean resultsBeanForCollectionMemberFeed;
    private final GenerateBookFeedService generateBookFeedService;
    private final GenerateCollectionMemberFeedService generateCollectionMemberFeedService;
    private final UploadFeedService uploadFeedService;
    private final EmailNotificationService emailNotificationService;
    private final IResourceLookUp resourceLookUp;
    private static Logger logger = Logger.getLogger(FeedGenerator.class);
    private String collectionFeedName;
    private String bookFeedName;
    private String collectionMemberFeedName;

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

        try {
            logger.info("**** Phase 1:- Fetching feed data has been started. ****");
            collectionFeedDataList = collectionFeedDataService.getCollectionFeedData();
            resultsBean = bookAndCollectionMemberFeedDataService.getBookAndCollectionMemberFeedData(resourceLookUp
                    .getResource("get.article.metadata.function"));
            resultsBeanForCollectionMemberFeed = bookAndCollectionMemberFeedDataService
                    .getBookAndCollectionMemberFeedData(resourceLookUp
                            .getResource("get.collection.member.feed.data.function"));
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

    void generateFeed() throws Exception {
        logger.info("**** Phase 2:- Feed generation has been started. ****");
        String currentDateInYYYYMMDDFormat = getDateInYYYYMMDDFormat();
        collectionFeedName = resourceLookUp.getResource("collection") + currentDateInYYYYMMDDFormat
                + resourceLookUp.getResource("file.type");
        generateCollectionFeedService.generateCollectionFeed(collectionFeedName,
                resourceLookUp.getResource("file.location"), collectionFeedDataList);

        bookFeedName = resourceLookUp.getResource("book") + currentDateInYYYYMMDDFormat
                + resourceLookUp.getResource("file.type");
        Map<String, String> groupCodes = generateBookFeedService.getCollectionGroupMap();
        generateBookFeedService.generateMpsBookFeed(bookFeedName, resourceLookUp.getResource("file.location"),
                resultsBean, groupCodes);
        collectionMemberFeedName = resourceLookUp.getResource("collection.member") + currentDateInYYYYMMDDFormat
                + resourceLookUp.getResource("file.type");
        generateCollectionMemberFeedService.generateCollctionMemberFeed(collectionMemberFeedName,
                resourceLookUp.getResource("file.location"), resultsBeanForCollectionMemberFeed);

        logger.info("**** Phase 2:- Feed generation has been completed. ****");
        uploadFeed();
    }

    /* This method will use to upload all three feeds on FTP location */

    void uploadFeed() throws Exception {
        logger.info("**** Phase 3:- Feed uploading has been started. ****");
        if (uploadFeedService.feedsUploadOperation(collectionFeedName, bookFeedName, collectionMemberFeedName)) {
            logger.info("**** Phase 3:- Feed uploading has been completed. ****");
            emailNotificationWhenFeedsGenerate();
        } else {
            emailNotificationWhenFeedsDonotGenerate();
        }
    }

    /* This method will use to send email notification when feeds generate successfully. */

    void emailNotificationWhenFeedsGenerate() throws Exception {
        logger.info("**** Phase 4:- E-mail notification has been started. ****");
        emailNotificationService.sendEmailNotification(resourceLookUp.getResource("mail.to"),
                resourceLookUp.getResource("mail.subject"), resourceLookUp.getResource("mail.body"));
        logger.info("**** Phase 4:- E-mail notification has been completed. ****");
    }

    /* This method will use to send email notification when feeds does not generate successfully. */

    void emailNotificationWhenFeedsDonotGenerate() throws Exception {
        logger.info("**** Phase 4:- E-mail notification has been started. ****");
        emailNotificationService.sendEmailNotification(resourceLookUp.getResource("feed.generation.failure.mail.to"),
                resourceLookUp.getResource("feed.generation.failure.mail.subject"),
                resourceLookUp.getResource("feed.generation.failure.mail.body"));
        logger.info("**** Phase 4:- E-mail notification has been completed. ****");
    }

    String getDateInYYYYMMDDFormat() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(resourceLookUp.getResource("date.format"));
        return sdf.format(Calendar.getInstance().getTime());
    }

}
