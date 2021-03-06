package com.nature.feeds.serviceimpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.csvreader.CsvWriter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.ItemBean;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.service.GenerateBookFeedService;
import com.nature.feeds.util.DBUtil;

public class GenerateBookFeedServiceImpl implements GenerateBookFeedService, Comparator<ItemBean> {

    private PreparedStatement stmt;
    private ResultSet rs;
    private final IResourceLookUp resourceLookUp;
    private Connection connection;
    private final DBUtil dBUtil;

    @Inject
    public GenerateBookFeedServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp, DBUtil dBUtil) {
        this.resourceLookUp = resourceLookUp;
        this.dBUtil = dBUtil;
    }

    /* This method will use to generate book feed. */
    @Override
    public void generateMpsBookFeed(String fileName, String filePath, ResultsBean feedDataBeans,
            Map<String, String> groupCodes) throws IOException, Exception {

        File file = new File(filePath + resourceLookUp.getResource("file.location.seperator") + fileName);
        CsvWriter csvFile = new CsvWriter(new FileWriter(file, true), ',');
        try {
            List<ItemBean> bookFeedDataWithGroupingList = getBookFeedDataWithGrouping(feedDataBeans, groupCodes);
            Collections.sort(bookFeedDataWithGroupingList, new GenerateBookFeedServiceImpl(resourceLookUp, dBUtil));
            createLabel(csvFile, getBookFeedsHeaderList());
            createBookContent(csvFile, bookFeedDataWithGroupingList);
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

    private List<String> getBookFeedsHeaderList() throws Exception {
        List<String> headerList = new ArrayList<String>();
        headerList.add(resourceLookUp.getResource("book.id"));
        headerList.add(resourceLookUp.getResource("book.title"));
        headerList.add(resourceLookUp.getResource("grouping"));
        headerList.add(resourceLookUp.getResource("publisher"));
        headerList.add(resourceLookUp.getResource("book.isbn"));
        headerList.add(resourceLookUp.getResource("platform"));
        headerList.add(resourceLookUp.getResource("doi"));
        return headerList;
    }

    private void createBookContent(CsvWriter csvFile, List<ItemBean> bookFeedDataWithGroupingList) throws IOException,
            Exception {

        ItemBean bean;

        if ((bookFeedDataWithGroupingList != null) && (bookFeedDataWithGroupingList.size() > 0)) {
            for (int index = 0; index < bookFeedDataWithGroupingList.size(); index++) {
                bean = bookFeedDataWithGroupingList.get(index);
                csvFile.write(bean.getThirteenDigitIsbn());
                csvFile.write(bean.getTitle());
                csvFile.write(bean.getGrouping());
                csvFile.write(resourceLookUp.getResource("palgrave.macmillan"));
                csvFile.write(bean.getThirteenDigitIsbn());
                csvFile.write(resourceLookUp.getResource("palgrave.connect"));
                csvFile.write(bean.getDoi());
                csvFile.endRecord();
            }
        }
    }

    @Override
    public Map<String, String> getCollectionGroupMap() throws Exception {
        Map<String, String> groupCodes = new HashMap<String, String>();
        Map<String, String> groupIdsByCode = getPCProductGroupIds();
        Map<String, String> groupNamesById = getPCProductGroups();

        for (Entry<String, String> codeIdEntry : groupIdsByCode.entrySet()) {
            groupCodes.put(codeIdEntry.getKey(), groupNamesById.get(codeIdEntry.getValue()));
        }
        return groupCodes;
    }

    private Map<String, String> getPCProductGroupIds() throws Exception {
        Map<String, String> productGroupByGroupCodeMap = new HashMap<String, String>();
        try {
            String query = " SELECT product_group_id,product_group_code "
                    + " FROM product_group ORDER BY product_group.product_group_code ";
            connection = dBUtil.openConnection();
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    productGroupByGroupCodeMap.put(rs.getString("product_group_code"),
                            Integer.toString(rs.getInt("product_group_id")));
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            dBUtil.closeConnection(connection);
        }
        return productGroupByGroupCodeMap;

    }

    private Map<String, String> getPCProductGroups() throws Exception {
        Map<String, String> productGroupByIdMap = new HashMap<String, String>();
        try {
            String query = " SELECT product_group_id,product_group_desc "
                    + " FROM product_group ORDER BY product_group.product_group_code ";
            connection = dBUtil.openConnection();
            stmt = connection.prepareStatement(query);
            rs = stmt.executeQuery();
            if (rs != null) {
                while (rs.next()) {
                    productGroupByIdMap.put(Integer.toString(rs.getInt("product_group_id")),
                            rs.getString("product_group_desc"));
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            dBUtil.closeConnection(connection);
        }
        return productGroupByIdMap;
    }

    private String getGrouping(String collectionWorkId, Map<String, String> groupCodes) throws Exception {
        String groupCode = groupCodes.get(collectionWorkId);
        if (groupCode.equalsIgnoreCase(resourceLookUp.getResource("groupcode.replace.this"))) {
            return resourceLookUp.getResource("groupcode.replace.with");
        }
        return groupCode;
    }

    private List<ItemBean> getBookFeedDataWithGrouping(ResultsBean resultsBean, Map<String, String> groupCodes)
            throws Exception {

        ItemBean itemBean;
        List<ItemBean> bookFeedDataWithGroupingList = null;

        if (resultsBean != null) {
            bookFeedDataWithGroupingList = new ArrayList<ItemBean>();
            for (int index = 0; index < resultsBean.getItems().size(); index++) {
                itemBean = resultsBean.getItems().get(index);
                itemBean.setGrouping(getGrouping(itemBean.getCollections().get(0).getCollectionWorkid(), groupCodes));
                bookFeedDataWithGroupingList.add(itemBean);
            }
        }
        return bookFeedDataWithGroupingList;
    }

    @Override
    public int compare(ItemBean itemBean1, ItemBean itemBean2) {
        return (itemBean1.getGrouping().compareTo(itemBean2.getGrouping()));
    }

}
