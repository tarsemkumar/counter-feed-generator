package com.nature.feeds.serviceimpl;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.ResultsBean;

public class GenerateCollectionMemberFeedServiceImplTest {

    private GenerateCollectionMemberFeedServiceImpl generateCollectionMemberFeedServiceImpl;

    @Mock
    private IResourceLookUp mockIResourceLookUp;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        generateCollectionMemberFeedServiceImpl = new GenerateCollectionMemberFeedServiceImpl(mockIResourceLookUp);

    }

    @Test
    public void generateCollctionMemberFeed() throws IOException, Exception {

        when(mockIResourceLookUp.getResource("collection.id")).thenReturn("collection.id");
        when(mockIResourceLookUp.getResource("book.id")).thenReturn("book.id");
        when(mockIResourceLookUp.getResource("byo")).thenReturn("byo");
        when(mockIResourceLookUp.getResource("file.location.seperator")).thenReturn("/");
        ResultsBean resultsBean = new ResultsBean();
        generateCollectionMemberFeedServiceImpl.generateCollctionMemberFeed("collectionmemberyyyyMMdd", "/feed",
                resultsBean);
        assertEquals(mockIResourceLookUp.getResource("byo"), "byo");

    }

}
