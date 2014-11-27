package com.nature.feeds.serviceimpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import com.nature.feeds.service.GenerateCollectionMemberFeedService;
import com.util.FeedsLogger;

public class GenerateCollectionMemberFeedServiceImpl implements GenerateCollectionMemberFeedService {

    private WritableCellFormat arial;
    private WritableCellFormat arialBoldUnderline;
    private final IResourceLookUp resourceLookUp;

    @Inject
    public GenerateCollectionMemberFeedServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to generate collection member feed. */

    @Override
    public void generateCollctionMemberFeed(String fileName, String filePath, ResultsBean beans) throws Exception {
        File file = new File(filePath + resourceLookUp.getResource("file.location.seperator") + fileName);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            workbook.createSheet(fileName, 0);
            WritableSheet excelSheet = workbook.getSheet(0);
            createLabel(excelSheet, getCollectionMemberFeedsHeaderList());
            createCollectionMemberContent(excelSheet, getNonByoTitleList(beans));
            workbook.write();
            FeedsLogger.INFO.info("\n**** Collection Member feed has been generated. ****");
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private List<String> getCollectionMemberFeedsHeaderList() throws Exception {
        List<String> headerList = new ArrayList<String>();
        headerList.add(resourceLookUp.getResource("collection.id"));
        headerList.add(resourceLookUp.getResource("book.id"));
        return headerList;
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

    private void createCollectionMemberContent(WritableSheet sheet, List<ItemBean> nonByoTitleList)
            throws WriteException, RowsExceededException, Exception {
        ItemBean bean;

        if ((nonByoTitleList != null) && (nonByoTitleList.size() > 0)) {
            for (int index = 0; index < nonByoTitleList.size(); index++) {
                bean = nonByoTitleList.get(index);
                sheet.setColumnView(0, 20);
                addNumber(sheet, 0, index + 1, bean.getCollections().get(0).getCollectionIsbn());
                sheet.setColumnView(1, 20);
                addNumber(sheet, 1, index + 1, bean.getThirteenDigitIsbn());
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

    private void addNumber(WritableSheet sheet, int column, int row, String s) throws WriteException,
            NumberFormatException, Exception {
        Number number;
        number = new Number(column, row, Double.parseDouble(s), arial);
        sheet.addCell(number);
    }
}
