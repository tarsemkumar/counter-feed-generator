package com.nature.feeds.serviceimpl;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.util.DBUtil;

public class GenerateBookFeedServiceImplTest {

    @Mock
    private IResourceLookUp mockIResourceLookUp;
    @Mock
    private DBUtil mockDBUtil;

    private GenerateBookFeedServiceImpl generateBookFeedServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        generateBookFeedServiceImpl = new GenerateBookFeedServiceImpl(mockIResourceLookUp, mockDBUtil);

    }

    @Test
    public void generateMpsBookFeed() throws IOException, Exception {
        when(mockIResourceLookUp.getResource("file.location.seperator")).thenReturn("/");
        when(mockIResourceLookUp.getResource("collection.id")).thenReturn("collection.id");
        when(mockIResourceLookUp.getResource("collection.title")).thenReturn("collection.title");
        when(mockIResourceLookUp.getResource("grouping")).thenReturn("grouping");
        when(mockIResourceLookUp.getResource("publisher")).thenReturn("publisher");
        when(mockIResourceLookUp.getResource("collection.isbn")).thenReturn("collection.isbn");
        when(mockIResourceLookUp.getResource("platform")).thenReturn("platform");
        when(mockIResourceLookUp.getResource("palgrave.macmillan")).thenReturn("palgrave.macmillan");
        when(mockIResourceLookUp.getResource("palgrave.connect")).thenReturn("palgrave.connect");
        ResultsBean resultsBean = new ResultsBean();
        Map<String, String> groupCodes = new HashMap<String, String>();
        generateBookFeedServiceImpl.generateMpsBookFeed("bookyyyyMMdd", "/feed", resultsBean, groupCodes);
        assertEquals(mockIResourceLookUp.getResource("file.location.seperator"), "/");

    }

}
