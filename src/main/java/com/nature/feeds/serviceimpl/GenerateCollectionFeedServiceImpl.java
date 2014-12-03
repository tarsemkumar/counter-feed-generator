package com.nature.feeds.serviceimpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvWriter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.service.GenerateCollectionFeedService;

public class GenerateCollectionFeedServiceImpl implements GenerateCollectionFeedService {

    private final IResourceLookUp resourceLookUp;

    @Inject
    public GenerateCollectionFeedServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to generate collection feed. */
    @Override
    public void generateCollectionFeed(String fileName, String filePath, List<CollectionBean> collectionFeedDataList)
            throws IOException, Exception {

        File directory = new File(filePath);
        if (directory.exists() == Boolean.FALSE) {
            directory.mkdir();
        }
        File file = new File(filePath + resourceLookUp.getResource("file.location.seperator") + fileName);
        CsvWriter csvFile = new CsvWriter(new FileWriter(file, true), ',');
        try {
            createLabel(csvFile, getCollectionFeedsHeaderList());
            createCollectionContent(csvFile, collectionFeedDataList);
        } finally {
            if (csvFile != null) {
                csvFile.flush();
                csvFile.close();
            }
        }
    }

    private void createLabel(CsvWriter csvFile, List<String> headerList) throws IOException, Exception {

        for (int index = 0; index < headerList.size(); index++) {
            csvFile.write(headerList.get(index));
        }
        csvFile.endRecord();
    }

    private List<String> getCollectionFeedsHeaderList() throws Exception {
        List<String> headerList = new ArrayList<String>();
        headerList.add(resourceLookUp.getResource("collection.id"));
        headerList.add(resourceLookUp.getResource("collection.title"));
        headerList.add(resourceLookUp.getResource("grouping"));
        headerList.add(resourceLookUp.getResource("publisher"));
        headerList.add(resourceLookUp.getResource("collection.isbn"));
        headerList.add(resourceLookUp.getResource("platform"));
        return headerList;
    }

    private void createCollectionContent(CsvWriter csvFile, List<CollectionBean> collectionFeedDataList)
            throws IOException, Exception {
        CollectionBean bean;
        if ((collectionFeedDataList != null) && (collectionFeedDataList.size() > 0)) {
            for (int index = 0; index < collectionFeedDataList.size(); index++) {
                bean = collectionFeedDataList.get(index);
                csvFile.write(bean.getIsbn());
                csvFile.write(bean.getProductDesc());
                csvFile.write(bean.getProductGroupDesc());
                csvFile.write(resourceLookUp.getResource("palgrave.macmillan"));
                csvFile.write(bean.getIsbn());
                csvFile.write(resourceLookUp.getResource("palgrave.connect"));
                csvFile.endRecord();
            }
        }
    }
}
