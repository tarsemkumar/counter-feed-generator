package com.nature.feeds.serviceimpl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nature.components.extractor.MarklogicDataExtractorRequestInfo;
import com.nature.components.extractor.service.ExternalServiceCaller;
import com.nature.components.extractor.service.ExternalServiceCallerException;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.ResultsBean;
import com.nature.feeds.exception.FatalException;

public class BookAndCollectionMemberFeedDataServiceImplTest {

    private BookAndCollectionMemberFeedDataServiceImpl bookAndCollectionMemberFeedDataServiceImpl;
    @Mock
    private ExternalServiceCaller mockExternalServiceCaller;
    @Mock
    private IResourceLookUp mockIResourceLookUp;
    @Mock
    private MarklogicDataExtractorRequestInfo mockMarklogicDataExtractorRequestInfo;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void getBookAndCollectionMemberFeedData() throws Exception {
        bookAndCollectionMemberFeedDataServiceImpl = new BookAndCollectionMemberFeedDataServiceImpl(
                mockExternalServiceCaller, mockIResourceLookUp) {
            @Override
            protected String invokeModule(String moduleNamespace, String moduleURI, String functionName,
                    Object... parameters) throws FatalException, ExternalServiceCallerException {
                return "<results><item><thirteen-digit-isbn>9780230288713</thirteen-digit-isbn><doi>10.1057/9780230288713</doi><title>The Politics of Cultural Work</title><collection><collection-acronym>SOCCULSTUDBACK</collection-acronym><collection-isbn>9780230283527</collection-isbn><collection-workid>488815</collection-workid></collection></item></results>";
            }
        };
        when(mockIResourceLookUp.getResource("admin.module.namespace")).thenReturn("admin");
        when(mockIResourceLookUp.getResource("admin.module.uri")).thenReturn("ebooks/ebooks_admin.xqy");
        when(mockIResourceLookUp.getResource("get.article.metadata.function"))
                .thenReturn("getTitlesDetailsForMpsFeeds");
        when(mockIResourceLookUp.getResource("get.collection.member.feed.data.function")).thenReturn(
                "getCollectionMemberFeedData");

        ResultsBean resultsBean = bookAndCollectionMemberFeedDataServiceImpl
                .getBookAndCollectionMemberFeedData("getTitlesDetailsForMpsFeeds");
        assertNotNull(resultsBean);
        assertEquals("Book & Collection Member Feed data", "9780230288713", resultsBean.getItems().get(0)
                .getThirteenDigitIsbn());
    }
}
