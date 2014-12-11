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
import com.nature.feeds.bean.ItemBean;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.service.GenerateCollectionMemberFeedService;

public class GenerateCollectionMemberFeedServiceImpl implements GenerateCollectionMemberFeedService {

    private final IResourceLookUp resourceLookUp;

    @Inject
    public GenerateCollectionMemberFeedServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to generate collection member feed. */

    @Override
    public void generateCollctionMemberFeed(String fileName, String filePath, ResultsBean beans) throws IOException,
            Exception {
        File file = new File(filePath + resourceLookUp.getResource("file.location.seperator") + fileName);
        CsvWriter csvFile = new CsvWriter(new FileWriter(file, true), ',');
        try {
            createLabel(csvFile, getCollectionMemberFeedsHeaderList());
            createCollectionMemberContent(csvFile, getNonByoTitleList(beans));
        } finally {
            if (csvFile != null) {
                csvFile.flush();
                csvFile.close();
            }
        }
    }

    private List<String> getCollectionMemberFeedsHeaderList() throws Exception {
        List<String> headerList = new ArrayList<String>();
        headerList.add(resourceLookUp.getResource("collection.id"));
        headerList.add(resourceLookUp.getResource("book.id"));
        return headerList;
    }

    private void createLabel(CsvWriter csvFile, List<String> headerList) throws IOException, Exception {
        for (int index = 0; index < headerList.size(); index++) {
            csvFile.write(headerList.get(index));
        }
        csvFile.endRecord();
    }

    private void createCollectionMemberContent(CsvWriter csvFile, List<ItemBean> nonByoTitleList) throws IOException,
            Exception {

        ItemBean bean;

        if ((nonByoTitleList != null) && (nonByoTitleList.size() > 0)) {
            for (int index = 0; index < nonByoTitleList.size(); index++) {
                bean = nonByoTitleList.get(index);
                if (!(bean.getCollections().get(0).getCollectionIsbn().equals(resourceLookUp
                        .getResource("collection.id.donot.add.in.feed")))) {
                    csvFile.write(bean.getCollections().get(0).getCollectionIsbn());
                    csvFile.write(bean.getThirteenDigitIsbn());
                    csvFile.endRecord();
                }
            }
        }
    }

    private List<ItemBean> getNonByoTitleList(ResultsBean beans) throws Exception {
        List<ItemBean> nonByoTitleList = new ArrayList<ItemBean>();
        int i = 0;
        if (beans != null) {
            for (ItemBean bean : beans.getItems()) {

                if (!(resourceLookUp.getResource("byo")).equalsIgnoreCase(bean.getCollections().get(0)
                        .getCollectionAcronym())) {
                    i = i + 1;
                    nonByoTitleList.add(bean);
                }
            }
        }
        return nonByoTitleList;
    }

}
