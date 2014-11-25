package com.nature.feeds.serviceimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.nature.feeds.service.UploadFeedService;
import com.util.Constants;

public class UploadFeedServiceImpl implements UploadFeedService {

    ResourceBundle ftpResources = ResourceBundle.getBundle("DatabaseResources");
    ResourceBundle fileUploadResources = ResourceBundle.getBundle("ApplicationResources");
    FTPClient ftpClient = null;

    /* This method will use to start feeds uploading process */

    @Override
    public Boolean uploadFeedsOnFTPLocation(ArrayList<String> todaysFeedName, ArrayList<String> yesterdaysFeedNames)
            throws Exception {

        Boolean status = Boolean.FALSE;
        try {
            ftpClient = new FTPClient();
            status = setFtpConnection(todaysFeedName, yesterdaysFeedNames);
            if (status == Boolean.TRUE) {
                status = uploadTodaysFeedsOnFTPLocation(todaysFeedName);
            }
            if (status == Boolean.TRUE) {
                status = deleteYesterdaysFeedsFromFTPLocation(yesterdaysFeedNames);
                deleteFeedsFromLocalLocation();
            }

        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }

        return status;
    }

    /* This method will use to create connection with FTP location */

    private Boolean setFtpConnection(ArrayList<String> todaysFeedsName, ArrayList<String> yesterdaysFeedNames)
            throws Exception {

        Boolean ftpConnectionStatus = Boolean.FALSE;

        ftpClient.connect(ftpResources.getString("ftp.host"), Integer.parseInt(ftpResources.getString("ftp.port")));
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            Constants.INFO.info("\n**** FTP connection creation operation failed. Server reply code: " + replyCode
                    + " . ****");
        }
        boolean success = ftpClient.login(ftpResources.getString("ftp.username"),
                ftpResources.getString("ftp.password"));
        if (!success) {
            Constants.INFO.info("\n**** FTP login operation failed. ****");
        } else {
            ftpConnectionStatus = Boolean.TRUE;
            Constants.INFO.info("\n**** FTP connection has been created. ****");
        }

        return ftpConnectionStatus;
    }

    /* This method will use to upload current feeds in FTP location */

    private Boolean uploadTodaysFeedsOnFTPLocation(ArrayList<String> todaysFeedNames) throws FileNotFoundException,
            IOException, Exception {

        File localFile = null;
        String remoteFile = null;
        InputStream inputStream = null;
        Boolean feedUploadStatus = Boolean.FALSE;

        try {
            if ((todaysFeedNames != null) && (todaysFeedNames.size() > 0)) {
                ftpClient.makeDirectory(ftpResources.getString("ftp.remote.path"));

                /* These three lines are very important during feeds uploading */
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                for (int index = 0; index < todaysFeedNames.size(); index++) {
                    localFile = new File(fileUploadResources.getString("file.location")
                            + fileUploadResources.getString("file.location.seperator") + todaysFeedNames.get(index));
                    remoteFile = ftpResources.getString("ftp.remote.path") + todaysFeedNames.get(index);
                    inputStream = new FileInputStream(localFile);
                    boolean done = ftpClient.storeFile(remoteFile, inputStream);
                    inputStream.close();
                    if (done) {
                        Constants.INFO.info("\n**** " + todaysFeedNames.get(index) + " file has been uploaded. ****");
                        feedUploadStatus = Boolean.TRUE;
                    } else {
                        Constants.INFO.info("\n**** " + todaysFeedNames.get(index)
                                + " file is not uploaded successfully. ****");
                        feedUploadStatus = Boolean.FALSE;
                        break;
                    }
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return feedUploadStatus;
    }

    /* This method will use to delete yesterday's feeds from FTP location */

    private Boolean deleteYesterdaysFeedsFromFTPLocation(ArrayList<String> yesterdaysFeedNames) throws Exception {

        Boolean feedDeletionStatus = Boolean.FALSE;
        if ((yesterdaysFeedNames != null) && (yesterdaysFeedNames.size() > 0)) {
            for (int index = 0; index < yesterdaysFeedNames.size(); index++) {
                Boolean done = ftpClient.deleteFile(ftpResources.getString("ftp.remote.path")
                        + yesterdaysFeedNames.get(index));
                if (done) {
                    feedDeletionStatus = Boolean.TRUE;
                    Constants.INFO.info("\n**** " + yesterdaysFeedNames.get(index) + " file has been deleted. ****");
                } else {
                    feedDeletionStatus = Boolean.FALSE;
                    Constants.INFO.info("\n**** " + yesterdaysFeedNames.get(index)
                            + " file is not deleted successfully. ****");
                    break;
                }
            }
        }
        return feedDeletionStatus;
    }

    /* This method will use to delete feeds from local location */

    private void deleteFeedsFromLocalLocation() throws IOException {
        Constants.INFO.info("\n**** Feeds deletion from local location has been started. ****");
        FileUtils.cleanDirectory(new File(fileUploadResources.getString("file.location")
                + fileUploadResources.getString("file.location.seperator")));
        Constants.INFO.info("\n***  Feeds deletion from local location has been completed. ****");
    }
}
