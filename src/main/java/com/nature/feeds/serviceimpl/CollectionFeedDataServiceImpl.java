package com.nature.feeds.serviceimpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.service.CollectionFeedDataService;

public class CollectionFeedDataServiceImpl implements CollectionFeedDataService {

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;
    private List<CollectionBean> bookAndCollectionMemberFeedDataList;
    private CollectionBean collectionBean;
    private final IResourceLookUp resourceLookUp;

    @Inject
    public CollectionFeedDataServiceImpl(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    /* This method will use to get Collection feed data from Mysql data base */

    @Override
    public List<CollectionBean> getCollectionFeedData() throws Exception {
        try {
            Class.forName(resourceLookUp.getResource("jdbc.driver"));
            conn = DriverManager.getConnection(resourceLookUp.getResource("db.url"),
                    resourceLookUp.getResource("user"), resourceLookUp.getResource("pass"));
            String query = " SELECT product.isbn , product.product_desc , productGroup.product_group_desc "
                    + " FROM product AS product , product_group AS productGroup " + " WHERE "
                    + " product.product_code <> 'PALCONAFEE' AND " + " product.is_searchable = TRUE AND "
                    + " product.product_code <> 'BYO' AND "
                    + " product.product_group_id = productGroup.product_group_id " + " ORDER BY product.product_code ";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            if (rs != null) {
                bookAndCollectionMemberFeedDataList = new ArrayList<CollectionBean>();
                while (rs.next()) {
                    collectionBean = new CollectionBean();
                    collectionBean.setIsbn(rs.getString("isbn"));
                    collectionBean.setProductDesc(rs.getString("product_desc"));
                    collectionBean.setProductGroupDesc(rs.getString("product_group_desc"));
                    bookAndCollectionMemberFeedDataList.add(collectionBean);
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
        return bookAndCollectionMemberFeedDataList;
    }
}
