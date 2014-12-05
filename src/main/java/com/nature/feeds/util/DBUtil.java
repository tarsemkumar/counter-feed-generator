package com.nature.feeds.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nature.components.service.resources.IResourceLookUp;

public class DBUtil {

    private final IResourceLookUp resourceLookUp;

    @Inject
    public DBUtil(@Named("lib_resource_lookup") IResourceLookUp resourceLookUp) {
        this.resourceLookUp = resourceLookUp;
    }

    public Connection openConnection() throws ClassNotFoundException, SQLException, Exception {
        Class.forName(resourceLookUp.getResource("jdbc.driver"));
        return DriverManager.getConnection(resourceLookUp.getResource("db.url"), resourceLookUp.getResource("user"),
                resourceLookUp.getResource("pass"));
    }

    public void closeConnection(Connection connection) throws SQLException, Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
