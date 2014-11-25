package com.nature.feeds.serviceimpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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

import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.service.GenerateCollectionFeedService;
import com.util.Constants;

public class GenerateCollectionFeedServiceImpl implements GenerateCollectionFeedService {

    private WritableCellFormat arial;
    private WritableCellFormat arialBoldUnderline;
    ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources");

    /* This method will use to generate collection feed. */
    @Override
    public void generateCollectionFeed(String fileName, String filePath, List<CollectionBean> collectionFeedDataList)
            throws Exception {

        File directory = new File(filePath);
        if (directory.exists() == Boolean.FALSE) {
            directory.mkdir();
        }
        File file = new File(filePath + messages.getString("file.location.seperator") + fileName);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            workbook.createSheet(fileName, 0);
            WritableSheet excelSheet = workbook.getSheet(0);
            createLabel(excelSheet, getCollectionFeedsHeaderList());
            createCollectionContent(excelSheet, collectionFeedDataList);
            workbook.write();
            Constants.INFO.info("\n**** Collection Feed has been generated. ****");
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
        arialBoldUnderline.setWrap(false);

        // Write headers
        for (int index = 0; index < headerList.size(); index++) {
            addCaption(sheet, index, 0, headerList.get(index));
        }
    }

    private List<String> getCollectionFeedsHeaderList() throws Exception {
        List<String> headerList = new ArrayList<String>();
        headerList.add(messages.getString("collection.id"));
        headerList.add(messages.getString("collection.title"));
        headerList.add(messages.getString("grouping"));
        headerList.add(messages.getString("publisher"));
        headerList.add(messages.getString("collection.isbn"));
        headerList.add(messages.getString("platform"));
        return headerList;
    }

    private void addCaption(WritableSheet sheet, int column, int row, String s) throws WriteException, Exception {
        Label label;
        label = new Label(column, row, s, arialBoldUnderline);
        sheet.addCell(label);
    }

    private void createCollectionContent(WritableSheet sheet, List<CollectionBean> collectionFeedDataList)
            throws WriteException, RowsExceededException, NumberFormatException, Exception {
        CollectionBean bean;

        for (int index = 0; index < collectionFeedDataList.size(); index++) {
            bean = collectionFeedDataList.get(index);
            sheet.setColumnView(0, 20);
            addNumber(sheet, 0, index + 1, bean.getIsbn());
            sheet.setColumnView(1, 20);
            addLabel(sheet, 1, index + 1, bean.getProductDesc());
            sheet.setColumnView(2, 16);
            addLabel(sheet, 2, index + 1, bean.getProductGroupDesc());
            sheet.setColumnView(3, 20);
            addLabel(sheet, 3, index + 1, messages.getString("palgrave.macmillan"));
            sheet.setColumnView(4, 16);
            addNumber(sheet, 4, index + 1, bean.getIsbn());
            sheet.setColumnView(5, 16);
            addLabel(sheet, 5, index + 1, messages.getString("palgrave.connect"));
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
}
