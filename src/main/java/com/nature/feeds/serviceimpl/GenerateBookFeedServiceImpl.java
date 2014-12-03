package com.nature.feeds.serviceimpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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

public class GenerateBookFeedServiceImpl implements GenerateBookFeedService {

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;
    private final IResourceLookUp resourceLookUp;

    @Inject
    public GenerateBookFeedServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to generate book feed. */
    @Override
    public void generateMpsBookFeed(String fileName, String filePath, ResultsBean feedDataBeans,
            Map<String, String> groupCodes) throws IOException, Exception {

        File file = new File(filePath + resourceLookUp.getResource("file.location.seperator") + fileName);
        CsvWriter csvFile = new CsvWriter(new FileWriter(file, true), ',');
        try {
            createLabel(csvFile, getBookFeedsHeaderList());
            createBookContent(csvFile, feedDataBeans, groupCodes);
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

    private void createBookContent(CsvWriter csvFile, ResultsBean beans, Map<String, String> groupCodes)
            throws IOException, Exception {

        ItemBean bean;

        if (beans != null) {
            for (int index = 0; index < beans.getItems().size(); index++) {
                bean = beans.getItems().get(index);
                csvFile.write(bean.getThirteenDigitIsbn());
                csvFile.write(bean.getTitle());
                csvFile.write(getGrouping(bean.getCollections().get(0).getCollectionWorkid(), groupCodes));
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
            Class.forName(resourceLookUp.getResource("jdbc.driver"));
            conn = DriverManager.getConnection(resourceLookUp.getResource("db.url"),
                    resourceLookUp.getResource("user"), resourceLookUp.getResource("pass"));
            String query = " SELECT product_group_id,product_group_code "
                    + " FROM product_group ORDER BY product_group.product_group_code ";
            stmt = conn.prepareStatement(query);
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
            if (conn != null) {
                conn.close();
            }
        }
        return productGroupByGroupCodeMap;

    }

    private Map<String, String> getPCProductGroups() throws Exception {
        Map<String, String> productGroupByIdMap = new HashMap<String, String>();
        try {
            Class.forName(resourceLookUp.getResource("jdbc.driver"));
            conn = DriverManager.getConnection(resourceLookUp.getResource("db.url"),
                    resourceLookUp.getResource("user"), resourceLookUp.getResource("pass"));
            String query = " SELECT product_group_id,product_group_desc "
                    + " FROM product_group ORDER BY product_group.product_group_code ";
            stmt = conn.prepareStatement(query);
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
            if (conn != null) {
                conn.close();
            }
        }
        return productGroupByIdMap;
    }

    private String getGrouping(String collectionWorkId, Map<String, String> groupCodes) throws Exception {
        return groupCodes.get(collectionWorkId);
    }
}
