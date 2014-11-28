package com.nature.feeds.serviceimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.service.UploadFeedService;
import com.util.FeedsLogger;

public class UploadFeedServiceImpl implements UploadFeedService {

    private FTPClient ftpClient = null;
    private final IResourceLookUp resourceLookUp;

    @Inject
    public UploadFeedServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to start feeds uploading process */

    @Override
    public Boolean feedsUploadOperation(ArrayList<String> todaysFeedName, ArrayList<String> yesterdaysFeedNames)
            throws Exception {

        Boolean feedsUploadOperationStepsStatus = Boolean.FALSE;
        try {
            ftpClient = new FTPClient();
            feedsUploadOperationStepsStatus = setFtpConnection(todaysFeedName, yesterdaysFeedNames);
            if (feedsUploadOperationStepsStatus == Boolean.TRUE) {
                uploadTodaysFeedsOnFTPLocation(todaysFeedName);
                deleteYesterdaysFeedsFromFTPLocation(yesterdaysFeedNames);
                deleteFeedsFromLocalLocation();
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }

        return feedsUploadOperationStepsStatus;
    }

    /* This method will use to create connection with FTP location */

    private Boolean setFtpConnection(ArrayList<String> todaysFeedsName, ArrayList<String> yesterdaysFeedNames)
            throws Exception {

        Boolean ftpConnectionStatus = Boolean.FALSE;

        ftpClient.connect(resourceLookUp.getResource("ftp.host"),
                Integer.parseInt(resourceLookUp.getResource("ftp.port")));
        ftpClient.getReplyCode();
        boolean success = ftpClient.login(resourceLookUp.getResource("ftp.username"),
                resourceLookUp.getResource("ftp.password"));
        if (!success) {
            FeedsLogger.INFO.info("\n**** FTP login operation failed. ****");
        } else {
            ftpConnectionStatus = Boolean.TRUE;
            FeedsLogger.INFO.info("\n**** FTP connection has been created. ****");
        }

        return ftpConnectionStatus;
    }

    /* This method will use to upload current feeds in FTP location */

    private void uploadTodaysFeedsOnFTPLocation(ArrayList<String> todaysFeedNames) throws FileNotFoundException,
            IOException, Exception {

        File localFile = null;
        String remoteFile = null;
        InputStream inputStream = null;

        try {
            if ((todaysFeedNames != null) && (todaysFeedNames.size() > 0)) {
                ftpClient.makeDirectory(resourceLookUp.getResource("ftp.remote.path"));

                /* These three lines are very important during feeds uploading */
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                for (int index = 0; index < todaysFeedNames.size(); index++) {
                    localFile = new File(resourceLookUp.getResource("file.location")
                            + resourceLookUp.getResource("file.location.seperator") + todaysFeedNames.get(index));
                    remoteFile = resourceLookUp.getResource("ftp.remote.path") + todaysFeedNames.get(index);
                    inputStream = new FileInputStream(localFile);
                    boolean done = ftpClient.storeFile(remoteFile, inputStream);
                    inputStream.close();
                    FeedsLogger.INFO.info("\n**** " + todaysFeedNames.get(index) + " file has been uploaded. ****");
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /* This method will use to delete yesterday's feeds from FTP location */

    private void deleteYesterdaysFeedsFromFTPLocation(ArrayList<String> yesterdaysFeedNames) throws Exception {
        if ((yesterdaysFeedNames != null) && (yesterdaysFeedNames.size() > 0)) {
            for (int index = 0; index < yesterdaysFeedNames.size(); index++) {
                Boolean done = ftpClient.deleteFile(resourceLookUp.getResource("ftp.remote.path")
                        + yesterdaysFeedNames.get(index));

            }
        }
    }

    /* This method will use to delete feeds from local location */

    private void deleteFeedsFromLocalLocation() throws IOException {
        FileUtils.cleanDirectory(new File(resourceLookUp.getResource("file.location")
                + resourceLookUp.getResource("file.location.seperator")));
    }
}
