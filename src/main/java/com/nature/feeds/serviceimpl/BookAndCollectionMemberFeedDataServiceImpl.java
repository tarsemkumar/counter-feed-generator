package com.nature.feeds.serviceimpl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ResourceBundle;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import com.google.inject.Injector;
import com.nature.components.extractor.MarklogicDataExtractorRequestInfo;
import com.nature.components.extractor.service.ExternalServiceCaller;
import com.nature.components.extractor.service.ExternalServiceCallerException;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.bean.ItemBean;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.exception.FatalException;
import com.nature.feeds.service.BookAndCollectionMemberFeedDataService;

public class BookAndCollectionMemberFeedDataServiceImpl implements BookAndCollectionMemberFeedDataService {

    ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources");
    //private ExternalServiceCaller<MarklogicDataExtractorRequestInfo> externalServiceCaller ;
    private ExternalServiceCaller<MarklogicDataExtractorRequestInfo> externalServiceCaller;

    /* This method will use to get data from mark logic data base. */

    @Override
    @SuppressWarnings("unchecked")
    public ResultsBean getBookAndCollectionMemberFeedData(Injector injector) throws Exception {

        ResultsBean resultsBean = new ResultsBean();
        externalServiceCaller = injector.getInstance(ExternalServiceCaller.class);
        String xmlFeedData = getTitlesDetailsForMpsFeeds();
        resultsBean = feedDataDigest(xmlFeedData);
        return resultsBean;
    }

    private String getTitlesDetailsForMpsFeeds() throws FatalException, ExternalServiceCallerException {
        return invokeModule(messages.getString("admin.module.namespace"), messages.getString("admin.module.uri"),
                messages.getString("get.article.metadata.function"));
    }

    private String invokeModule(String moduleNamespace, String moduleURI, String functionName, Object... parameters)
            throws FatalException, ExternalServiceCallerException {
        return externalServiceCaller.callService(getMarklogicDataExtractorRequestInfo(moduleNamespace, moduleURI,
                functionName, parameters));
    }

    private MarklogicDataExtractorRequestInfo getMarklogicDataExtractorRequestInfo(String moduleNamespace,
            String moduleURI, String functionName, Object... parameters) {
        return new MarklogicDataExtractorRequestInfo(moduleNamespace, moduleURI, functionName, parameters);
    }

    private ResultsBean feedDataDigest(String xmlFeedData) throws IOException, SAXException {
        ResultsBean resultsBean = new ResultsBean();
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("results", ResultsBean.class);
        digester.addObjectCreate("results/item", ItemBean.class);
        digester.addBeanPropertySetter("results/item/thirteen-digit-isbn", "thirteenDigitIsbn");
        digester.addBeanPropertySetter("results/item/doi", "doi");
        digester.addBeanPropertySetter("results/item/title", "title");
        digester.addSetNext("results/item", "addItem");
        digester.addObjectCreate("results/item/collection", CollectionBean.class);
        digester.addBeanPropertySetter("results/item/collection/collection-acronym", "collectionAcronym");
        digester.addBeanPropertySetter("results/item/collection/collection-isbn", "collectionIsbn");
        digester.addBeanPropertySetter("results/item/collection/collection-workid", "collectionWorkid");
        digester.addSetNext("results/item/collection", "addCollection");
        resultsBean = (ResultsBean) digester.parse(new StringReader(xmlFeedData));
        return resultsBean;
    }
}
