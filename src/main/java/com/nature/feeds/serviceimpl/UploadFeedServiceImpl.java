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
import org.apache.commons.net.ftp.FTPFile;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.service.UploadFeedService;

public class UploadFeedServiceImpl implements UploadFeedService {

    private FTPClient ftpClient = null;
    private final IResourceLookUp resourceLookUp;

    @Inject
    public UploadFeedServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to start feeds uploading process */

    @Override
    public boolean feedsUploadOperation(String collectionFeedName, String bookFeedName, String collectionMemberFeedName)
            throws Exception {

        boolean feedsUploadOperationStepsStatus = false;
        try {
            ftpClient = new FTPClient();
            feedsUploadOperationStepsStatus = setFtpConnection();
            if (feedsUploadOperationStepsStatus) {
                ArrayList<String> todaysFeedNameList = getTodaysFeedNamesList(collectionFeedName, bookFeedName,
                        collectionMemberFeedName);
                uploadTodaysFeedsOnFTPLocation(todaysFeedNameList);
                deleteOldFeedsFromFTPLocation();
                deleteFolderFromFTPLocation();
                renameBackupFolderOnFTPLocation();
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

    private boolean setFtpConnection() throws Exception {

        ftpClient.connect(resourceLookUp.getResource("ftp.host"),
                Integer.parseInt(resourceLookUp.getResource("ftp.port")));
        return ftpClient.login(resourceLookUp.getResource("ftp.username"), resourceLookUp.getResource("ftp.password"));
    }

    /* This method will use to upload current feeds in FTP location */

    private void uploadTodaysFeedsOnFTPLocation(ArrayList<String> todaysFeedNames) throws FileNotFoundException,
            IOException, Exception {

        File localFile = null;
        String remoteFile = null;
        InputStream inputStream = null;

        try {
            if ((todaysFeedNames != null) && (todaysFeedNames.size() > 0)) {
                ftpClient.makeDirectory(resourceLookUp.getResource("ftp.backup.folder.path"));

                /* These three lines are very important during feeds uploading */
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                for (int index = 0; index < todaysFeedNames.size(); index++) {
                    localFile = new File(resourceLookUp.getResource("file.location")
                            + resourceLookUp.getResource("file.location.seperator") + todaysFeedNames.get(index));
                    remoteFile = resourceLookUp.getResource("ftp.backup.folder.path") + todaysFeedNames.get(index);
                    inputStream = new FileInputStream(localFile);
                    ftpClient.storeFile(remoteFile, inputStream);
                    inputStream.close();
                }
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    /* This method will use to delete old feeds from FTP location */

    private void deleteOldFeedsFromFTPLocation() throws Exception {
        ftpClient.changeWorkingDirectory(resourceLookUp.getResource("ftp.old.feeds.folder.path"));
        FTPFile[] oldFtpFeedList = ftpClient.listFiles();
        if ((oldFtpFeedList != null) && (oldFtpFeedList.length > 0)) {
            for (int index = 0; index < oldFtpFeedList.length; index++) {
                ftpClient.deleteFile(resourceLookUp.getResource("ftp.old.feeds.folder.path")
                        + oldFtpFeedList[index].getName());
            }
        }
    }

    /* This method will use to delete folder from ftp location */

    private void deleteFolderFromFTPLocation() throws Exception {
        ftpClient.removeDirectory(resourceLookUp.getResource("ftp.delete.old.feeds.folder.path"));
    }

    /* This method will use to rename backup folder */
    private void renameBackupFolderOnFTPLocation() throws Exception {
        ftpClient.rename(resourceLookUp.getResource("ftp.rename.backup.folder.path"),
                resourceLookUp.getResource("ftp.delete.old.feeds.folder.path"));
    }

    /* This method will use to delete feeds from local location */

    private void deleteFeedsFromLocalLocation() throws IOException {
        FileUtils.cleanDirectory(new File(resourceLookUp.getResource("file.location")
                + resourceLookUp.getResource("file.location.seperator")));
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
}
