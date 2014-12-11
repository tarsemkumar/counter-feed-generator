package com.nature.feeds.startup;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class FeedGeneratorTest {

    @Mock
    private CollectionFeedDataService mockCollectionFeedDataService;
    @Mock
    private BookAndCollectionMemberFeedDataService mockBookAndCollectionMemberFeedDataService;
    @Mock
    private GenerateCollectionFeedService mockGenerateCollectionFeedService;
    @Mock
    private GenerateBookFeedService mockGenerateBookFeedService;
    @Mock
    private GenerateCollectionMemberFeedService mockGenerateCollectionMemberFeedService;
    @Mock
    private UploadFeedService mockUploadFeedService;
    @Mock
    private EmailNotificationService mockEmailNotificationService;
    @Mock
    private IResourceLookUp mockResourceLookUp;
    private FeedGenerator feedGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void fetchFeedData() throws Exception {

        feedGenerator = new FeedGenerator(mockCollectionFeedDataService, mockGenerateCollectionFeedService,
                mockBookAndCollectionMemberFeedDataService, mockGenerateBookFeedService,
                mockGenerateCollectionMemberFeedService, mockUploadFeedService, mockEmailNotificationService,
                mockResourceLookUp) {

            @Override
            void generateFeed() throws Exception {
            }
        };

        List<CollectionBean> listCollectionFeedData = new ArrayList<CollectionBean>();
        when(mockCollectionFeedDataService.getCollectionFeedData()).thenReturn(listCollectionFeedData);
        ResultsBean resultsBean = new ResultsBean();
        when(
                mockBookAndCollectionMemberFeedDataService
                        .getBookAndCollectionMemberFeedData("getTitlesDetailsForMpsFeeds")).thenReturn(resultsBean);
        feedGenerator.fetchFeedData();
        verify(mockCollectionFeedDataService).getCollectionFeedData();
    }

    @Test
    public void generateFeed() throws Exception {
        feedGenerator = new FeedGenerator(mockCollectionFeedDataService, mockGenerateCollectionFeedService,
                mockBookAndCollectionMemberFeedDataService, mockGenerateBookFeedService,
                mockGenerateCollectionMemberFeedService, mockUploadFeedService, mockEmailNotificationService,
                mockResourceLookUp) {
            @Override
            void uploadFeed() throws Exception {
            }
        };

        when(mockResourceLookUp.getResource("date.format")).thenReturn("yyyyMMdd");
        when(mockResourceLookUp.getResource("collection")).thenReturn("collection");
        when(mockResourceLookUp.getResource("file.type")).thenReturn(".csv");
        when(mockResourceLookUp.getResource("book")).thenReturn("book");
        when(mockResourceLookUp.getResource("collection.member")).thenReturn("collectionmember");
        when(mockResourceLookUp.getResource("file.location")).thenReturn("/feed");

        List<CollectionBean> collectionFeedDataList = new ArrayList<CollectionBean>();
        mockGenerateCollectionFeedService.generateCollectionFeed("collection20141202", "/feed", collectionFeedDataList);
        Map<String, String> collectionGroupMap = new HashMap<String, String>();
        when(mockGenerateBookFeedService.getCollectionGroupMap()).thenReturn(collectionGroupMap);
        ResultsBean resultsBean = new ResultsBean();
        mockGenerateBookFeedService.generateMpsBookFeed("book20141202", "/feed", resultsBean, collectionGroupMap);
        mockGenerateCollectionMemberFeedService.generateCollctionMemberFeed("collectionmember20141202", "/feed",
                resultsBean);
        feedGenerator.generateFeed();
        verify(mockGenerateCollectionFeedService).generateCollectionFeed("collection20141202", "/feed",
                collectionFeedDataList);
    }

    @Test
    public void uploadFeed() throws Exception {
        feedGenerator = new FeedGenerator(mockCollectionFeedDataService, mockGenerateCollectionFeedService,
                mockBookAndCollectionMemberFeedDataService, mockGenerateBookFeedService,
                mockGenerateCollectionMemberFeedService, mockUploadFeedService, mockEmailNotificationService,
                mockResourceLookUp) {

            @Override
            void emailNotificationWhenFeedsGenerate() throws Exception {
            }
        };
        when(mockUploadFeedService.feedsUploadOperation(anyString(), anyString(), anyString())).thenReturn(true);
        feedGenerator.uploadFeed();
        verify(mockUploadFeedService).feedsUploadOperation(anyString(), anyString(), anyString());
    }

    @Test
    public void emailNotificationWhenFeedsGenerate() throws Exception {
        feedGenerator = new FeedGenerator(mockCollectionFeedDataService, mockGenerateCollectionFeedService,
                mockBookAndCollectionMemberFeedDataService, mockGenerateBookFeedService,
                mockGenerateCollectionMemberFeedService, mockUploadFeedService, mockEmailNotificationService,
                mockResourceLookUp) {
        };

        when(mockResourceLookUp.getResource("mail.to")).thenReturn("pradeep.kumar@adi-mps.com");
        when(mockResourceLookUp.getResource("mail.subject")).thenReturn("Feeds has generated successfully.");
        when(mockResourceLookUp.getResource("mail.body")).thenReturn("You can download feeds from ftp location.");
        mockEmailNotificationService.sendEmailNotification("ram.kumar@adi-mps.com",
                "Feeds has generated successfully.", "You can download feeds from given ftp location.");
        feedGenerator.emailNotificationWhenFeedsGenerate();
        verify(mockEmailNotificationService).sendEmailNotification("pradeep.kumar@adi-mps.com",
                "Feeds has generated successfully.", "You can download feeds from ftp location.");

    }

    @Test
    public void emailNotificationWhenFeedsDonotGenerate() throws Exception {
        feedGenerator = new FeedGenerator(mockCollectionFeedDataService, mockGenerateCollectionFeedService,
                mockBookAndCollectionMemberFeedDataService, mockGenerateBookFeedService,
                mockGenerateCollectionMemberFeedService, mockUploadFeedService, mockEmailNotificationService,
                mockResourceLookUp) {
        };

        when(mockResourceLookUp.getResource("feed.generation.failure.mail.to")).thenReturn(
                "att.developergroup@adi-mps.com");
        when(mockResourceLookUp.getResource("feed.generation.failure.mail.subject")).thenReturn(
                "Feeds has not generated successfully.");
        when(mockResourceLookUp.getResource("feed.generation.failure.mail.body")).thenReturn("Failuer message.");
        mockEmailNotificationService.sendEmailNotification("ajay.kumar@adi-mps.com",
                "Feeds has not generated successfully.", "Feeds generation fail.");
        feedGenerator.emailNotificationWhenFeedsDonotGenerate();
        verify(mockEmailNotificationService).sendEmailNotification("att.developergroup@adi-mps.com",
                "Feeds has not generated successfully.", "Failuer message.");

    }
}
