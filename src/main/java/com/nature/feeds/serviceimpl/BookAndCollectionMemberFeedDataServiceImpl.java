package com.nature.feeds.serviceimpl;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.extractor.MarklogicDataExtractorRequestInfo;
import com.nature.components.extractor.service.ExternalServiceCaller;
import com.nature.components.extractor.service.ExternalServiceCallerException;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.bean.ItemBean;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.exception.FatalException;
import com.nature.feeds.service.BookAndCollectionMemberFeedDataService;

public class BookAndCollectionMemberFeedDataServiceImpl implements BookAndCollectionMemberFeedDataService {

    private final ExternalServiceCaller<MarklogicDataExtractorRequestInfo> externalServiceCaller;
    private final IResourceLookUp resourceLookUp;

    @SuppressWarnings("unchecked")
    @Inject
    public BookAndCollectionMemberFeedDataServiceImpl(ExternalServiceCaller externalServiceCaller,
            @Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.externalServiceCaller = externalServiceCaller;
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to get data from mark logic data base. */

    @Override
    public ResultsBean getBookAndCollectionMemberFeedData() throws Exception {

        ResultsBean resultsBean = new ResultsBean();
        String xmlFeedData = getTitlesDetailsForMpsFeeds();
        resultsBean = feedDataDigest(xmlFeedData);
        return resultsBean;
    }

    private String getTitlesDetailsForMpsFeeds() throws FatalException, ExternalServiceCallerException {
        return invokeModule(resourceLookUp.getResource("admin.module.namespace"),
                resourceLookUp.getResource("admin.module.uri"),
                resourceLookUp.getResource("get.article.metadata.function"));
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
