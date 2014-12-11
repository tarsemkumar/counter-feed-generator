package com.nature.feeds.serviceimpl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.util.DBUtil;

public class CollectionFeedDataServiceImplTest {

    @Mock
    private IResourceLookUp mockResourceLookUp;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private DriverManager mockDriverManager;
    @Mock
    private DBUtil mockDBUtil;

    private CollectionFeedDataServiceImpl collectionFeedDataServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        collectionFeedDataServiceImpl = new CollectionFeedDataServiceImpl(mockDBUtil);
    }

    @Test
    public void getCollectionFeedData() throws Exception {

        String query = " SELECT product.isbn , product.product_desc , productGroup.product_group_desc "
                + " FROM product AS product , product_group AS productGroup " + " WHERE "
                + " product.product_code <> 'PALCONAFEE' AND " + " product.is_searchable = TRUE AND "
                + " product.product_code <> 'BYO' AND " + " product.product_group_id = productGroup.product_group_id "
                + " ORDER BY product.product_desc ";
        when(mockDBUtil.openConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(query)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("isbn")).thenReturn("isbn");
        when(mockResultSet.getString("product_desc")).thenReturn("product_desc");
        when(mockResultSet.getString("product_group_desc")).thenReturn("product_group_desc");
        collectionFeedDataServiceImpl.getCollectionFeedData();
        verify(mockResultSet).close();
        verify(mockPreparedStatement).close();
        verify(mockDBUtil).closeConnection(mockConnection);

    }
}
