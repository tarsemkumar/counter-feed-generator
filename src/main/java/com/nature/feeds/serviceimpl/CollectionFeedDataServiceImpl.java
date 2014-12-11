package com.nature.feeds.serviceimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.service.CollectionFeedDataService;
import com.nature.feeds.util.DBUtil;

public class CollectionFeedDataServiceImpl implements CollectionFeedDataService {

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;
    private List<CollectionBean> bookAndCollectionMemberFeedDataList;
    private CollectionBean collectionBean;
    private final DBUtil dBUtil;

    @Inject
    public CollectionFeedDataServiceImpl(DBUtil dBUtil) {
        this.dBUtil = dBUtil;

    }

    /* This method will use to get Collection feed data from Mysql data base */

    @Override
    public List<CollectionBean> getCollectionFeedData() throws Exception {
        try {
            String query = " SELECT product.isbn , product.product_desc , productGroup.product_group_desc "
                    + " FROM product AS product , product_group AS productGroup " + " WHERE "
                    + " product.product_code <> 'PALCONAFEE' AND " + " product.is_searchable = TRUE AND "
                    + " product.product_code <> 'BYO' AND "
                    + " product.product_group_id = productGroup.product_group_id " + " ORDER BY product.product_desc ";
            conn = dBUtil.openConnection();
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
            dBUtil.closeConnection(conn);
        }
        return bookAndCollectionMemberFeedDataList;
    }
}
