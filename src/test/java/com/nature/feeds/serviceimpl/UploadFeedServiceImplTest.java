package com.nature.feeds.serviceimpl;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nature.components.service.resources.IResourceLookUp;

public class UploadFeedServiceImplTest {

    private UploadFeedServiceImpl uploadFeedServiceImpl;

    @Mock
    private FTPClient mockFTPClient;
    @Mock
    private IResourceLookUp mockIResourceLookUp;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void feedsUploadOperation() throws Exception {
        uploadFeedServiceImpl = new UploadFeedServiceImpl(mockIResourceLookUp) {

            @Override
            boolean setFtpConnection() throws Exception {

                return true;
            }

            @Override
            void uploadTodaysFeedsOnFTPLocation(ArrayList<String> todaysFeedNames) throws FileNotFoundException,
                    IOException, Exception {

            }

            @Override
            void deleteOldFeedsFromFTPLocation() throws Exception {

            }

            @Override
            void deleteFolderFromFTPLocation() throws Exception {

            }

            @Override
            void renameBackupFolderOnFTPLocation() throws Exception {

            }

        };
        when(mockIResourceLookUp.getResource("file.location.seperator")).thenReturn("/");
        when(mockIResourceLookUp.getResource("file.location")).thenReturn("feed");
        boolean hasFeedUploaded = uploadFeedServiceImpl.feedsUploadOperation("collectionyyyyMMdd.cvs",
                "bookyyyyMMdd.cvs", "collectionmemberyyyyMMdd.cvs");
        assertEquals(hasFeedUploaded, true);

    }
}
