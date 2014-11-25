package com.nature.feeds.serviceimpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import com.nature.feeds.bean.CollectionBean;
import com.nature.feeds.service.CollectionFeedDataService;

public class CollectionFeedDataServiceImpl implements CollectionFeedDataService {

    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    List<CollectionBean> bookAndCollectionMemberFeedDataList = new ArrayList<CollectionBean>();
    CollectionBean collectionBean = null;
    ResourceBundle messages = ResourceBundle.getBundle("DatabaseResources");

    /* This method will use to get Collection feed data from Mysql data base */

    @Override
    public List<CollectionBean> getCollectionFeedData() throws Exception {
        try {
            Class.forName(messages.getString("jdbc.driver"));
            conn = DriverManager.getConnection(messages.getString("db.url"), messages.getString("user"),
                    messages.getString("pass"));
            StringBuilder query = new StringBuilder();
            query.append(" SELECT product.isbn , product.product_desc , productGroup.product_group_desc "
                    + " FROM product AS product , product_group AS productGroup " + " WHERE "
                    + " product.product_code <> 'PALCONAFEE' AND " + " product.is_searchable = TRUE AND "
                    + " product.product_code <> 'BYO' AND "
                    + " product.product_group_id = productGroup.product_group_id " + " ORDER BY product.product_code;");
            stmt = conn.prepareStatement(query.toString());
            rs = stmt.executeQuery();
            if (rs != null) {
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
