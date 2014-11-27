package com.nature.feeds.serviceimpl;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.ItemBean;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.service.GenerateBookFeedService;
import com.util.FeedsLogger;

public class GenerateBookFeedServiceImpl implements GenerateBookFeedService {

    private WritableCellFormat arialBoldUnderline;
    private WritableCellFormat arial;
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
            Map<String, String> groupCodes) throws Exception {

        File file = new File(filePath + resourceLookUp.getResource("file.location.seperator") + fileName);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            workbook.createSheet(fileName, 0);
            WritableSheet excelSheet = workbook.getSheet(0);
            createLabel(excelSheet, getBookFeedsHeaderList());
            createBookContent(excelSheet, feedDataBeans, groupCodes);
            workbook.write();
            FeedsLogger.INFO.info("\n**** Book Feed has been generated. ****");
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }

    }

    private void createLabel(WritableSheet sheet, List<String> headerList) throws WriteException, Exception {
        // Define a font type as arial with pt 10
        WritableFont arial10pt = new WritableFont(WritableFont.ARIAL, 10);
        // Define the cell format
        arial = new WritableCellFormat(arial10pt);

        // Create a bold font for header
        WritableFont areal10ptBoldUnderline = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        arialBoldUnderline = new WritableCellFormat(areal10ptBoldUnderline);
        // Automatically wrap the cells
        arialBoldUnderline.setWrap(true);

        // Write headers
        for (int index = 0; index < headerList.size(); index++) {
            addCaption(sheet, index, 0, headerList.get(index));
        }
    }

    private void addCaption(WritableSheet sheet, int column, int row, String s) throws WriteException, Exception {
        Label label;
        label = new Label(column, row, s, arialBoldUnderline);
        sheet.addCell(label);
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

    private void createBookContent(WritableSheet sheet, ResultsBean beans, Map<String, String> groupCodes)
            throws WriteException, RowsExceededException, Exception {

        ItemBean bean;
        int i = 0;

        if (beans != null) {
            for (int index = 0; index < beans.getItems().size(); index++) {
                bean = beans.getItems().get(index);
                sheet.setColumnView(0, 20);
                addNumber(sheet, 0, index + 1, bean.getThirteenDigitIsbn());
                sheet.setColumnView(1, 20);
                addLabel(sheet, 1, index + 1, bean.getTitle());
                sheet.setColumnView(2, 16);
                addLabel(sheet, 2, index + 1,
                        getGrouping(bean.getCollections().get(0).getCollectionWorkid(), groupCodes));
                sheet.setColumnView(3, 20);
                addLabel(sheet, 3, index + 1, resourceLookUp.getResource("palgrave.macmillan"));
                sheet.setColumnView(4, 16);
                addNumber(sheet, 4, index + 1, bean.getThirteenDigitIsbn());
                sheet.setColumnView(5, 16);
                addLabel(sheet, 5, index + 1, resourceLookUp.getResource("palgrave.connect"));
                sheet.setColumnView(6, 20);
                addLabel(sheet, 6, index + 1, bean.getDoi());
                i = i + 1;
            }
        }
    }

    private void addLabel(WritableSheet sheet, int column, int row, String s) throws WriteException, Exception {
        Label label;
        label = new Label(column, row, s, arial);
        sheet.addCell(label);
    }

    private void addNumber(WritableSheet sheet, int column, int row, String s) throws WriteException,
            NumberFormatException, Exception {
        Number number;
        number = new Number(column, row, Double.parseDouble(s), arial);
        sheet.addCell(number);
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
