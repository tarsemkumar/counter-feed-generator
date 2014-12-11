package com.nature.feeds.serviceimpl;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.CollectionBean;

public class GenerateCollectionFeedServiceImplTest {

    @Mock
    private IResourceLookUp mockIResourceLookUp;

    private GenerateCollectionFeedServiceImpl generateCollectionFeedServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        generateCollectionFeedServiceImpl = new GenerateCollectionFeedServiceImpl(mockIResourceLookUp);
    }

    @Test
    public void generateCollectionFeed() throws IOException, Exception {

        when(mockIResourceLookUp.getResource("file.location.seperator")).thenReturn("/");
        when(mockIResourceLookUp.getResource("collection.id")).thenReturn("collection.id");
        when(mockIResourceLookUp.getResource("collection.title")).thenReturn("collection.title");
        when(mockIResourceLookUp.getResource("grouping")).thenReturn("grouping");
        when(mockIResourceLookUp.getResource("publisher")).thenReturn("publisher");
        when(mockIResourceLookUp.getResource("collection.isbn")).thenReturn("collection.isbn");
        when(mockIResourceLookUp.getResource("platform")).thenReturn("platform");
        when(mockIResourceLookUp.getResource("palgrave.macmillan")).thenReturn("palgrave.macmillan");
        when(mockIResourceLookUp.getResource("palgrave.connect")).thenReturn("palgrave.connect");
        List<CollectionBean> collectionFeedList = new ArrayList<CollectionBean>();
        generateCollectionFeedServiceImpl.generateCollectionFeed("collectionyyyyMMdd", "/feed", collectionFeedList);
        assertEquals(mockIResourceLookUp.getResource("palgrave.connect"), "palgrave.connect");

    }
}
