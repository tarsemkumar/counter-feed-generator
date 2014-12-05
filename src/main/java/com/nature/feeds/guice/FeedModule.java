package com.nature.feeds.guice;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.marklogic.xcc.ContentSource;
import com.nature.components.extractor.service.ExternalMarklogicXccServiceCallerImpl;
import com.nature.components.extractor.service.ExternalServiceCaller;
import com.nature.components.marklogic.ContentStoreQuery;
import com.nature.components.marklogic.ContentStoreQueryImpl;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.components.service.resources.ResourceLookUp;
import com.nature.feeds.service.BookAndCollectionMemberFeedDataService;
import com.nature.feeds.service.CollectionFeedDataService;
import com.nature.feeds.service.EmailNotificationService;
import com.nature.feeds.service.GenerateBookFeedService;
import com.nature.feeds.service.GenerateCollectionFeedService;
import com.nature.feeds.service.GenerateCollectionMemberFeedService;
import com.nature.feeds.service.UploadFeedService;
import com.nature.feeds.serviceimpl.BookAndCollectionMemberFeedDataServiceImpl;
import com.nature.feeds.serviceimpl.CollectionFeedDataServiceImpl;
import com.nature.feeds.serviceimpl.EmailNotificationServiceImpl;
import com.nature.feeds.serviceimpl.GenerateBookFeedServiceImpl;
import com.nature.feeds.serviceimpl.GenerateCollectionFeedServiceImpl;
import com.nature.feeds.serviceimpl.GenerateCollectionMemberFeedServiceImpl;
import com.nature.feeds.serviceimpl.UploadFeedServiceImpl;
import com.nature.feeds.startup.FeedGenerator;
import com.nature.feeds.util.DBUtil;
import com.nature.marklogic.ConnectionBean;
import com.nature.marklogic.Connector;
import com.nature.marklogic.ConnectorImpl;
import com.nature.marklogic.ContentSourceProvider;
import com.nature.marklogic.QueryBuilder;

public class FeedModule implements Module {

    private static Logger logger = Logger.getLogger(UploadFeedServiceImpl.class);

    @Override
    public void configure(Binder binder) {

        binder.bind(CollectionFeedDataService.class).to(CollectionFeedDataServiceImpl.class);
        binder.bind(GenerateCollectionFeedService.class).to(GenerateCollectionFeedServiceImpl.class);
        binder.bind(BookAndCollectionMemberFeedDataService.class).to(BookAndCollectionMemberFeedDataServiceImpl.class);
        binder.bind(ExternalServiceCaller.class).to(ExternalMarklogicXccServiceCallerImpl.class);
        binder.bind(ContentStoreQuery.class).to(ContentStoreQueryImpl.class);
        //please check it after implementation
        binder.bind(GenerateBookFeedService.class).to(GenerateBookFeedServiceImpl.class);
        binder.bind(GenerateCollectionMemberFeedService.class).to(GenerateCollectionMemberFeedServiceImpl.class);
        binder.bind(UploadFeedService.class).to(UploadFeedServiceImpl.class);
        binder.bind(EmailNotificationService.class).to(EmailNotificationServiceImpl.class);
        binder.bind(QueryBuilder.class);
        binder.bind(FeedGenerator.class);
        binder.bind(DBUtil.class);
    }

    @Provides
    @Named("lib_resource_lookup")
    @Singleton
    private IResourceLookUp getRsourceLookup() {
        try {
            return new ResourceLookUp("ApplicationResources");
        } catch (IOException e) {
            logger.info("Exception reading properties", e);
        }
        return null;
    }

    @Provides
    @Named("connection_bean")
    @Singleton
    private ConnectionBean getConnectionBean(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        String host = resourceLookUp.getResource("marklogic.host");
        String port = resourceLookUp.getResource("marklogic.port");
        String user = resourceLookUp.getResource("marklogic.user");
        String pass = resourceLookUp.getResource("marklogic.password");
        String db = resourceLookUp.getResource("marklogic.db");
        return new ConnectionBean(host, port, user, pass, db);
    }

    @Provides
    @Named("content_source")
    @Singleton
    private ContentSource getContentSource(@Named("connection_bean") ConnectionBean connectionBean) {
        return new ContentSourceProvider(connectionBean).get();
    }

    @Provides
    @Singleton
    private Connector getConnector(@Named("content_source") ContentSource contentSource, QueryBuilder queryBuilder) {
        return new ConnectorImpl(contentSource, queryBuilder);
    }
}
