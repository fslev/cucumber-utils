package ro.cucumber.core.clients.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqlClient {
    private static int MAX_ROWS = 100;
    private Logger log = LogManager.getLogger();
    private String url;
    private String user;
    private String pwd;
    private String driverClassName;

    protected SqlClient(Builder builder) {
        this.url = builder.url;
        this.user = builder.user;
        this.pwd = builder.pwd;
        this.driverClassName = builder.driver;
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String, String>> executeQuery(String sql) {
        log.debug("---- SQL QUERY REQUEST ----");
        log.debug("Driver: {}", driverClassName);
        log.debug("Database url: {}", url);
        log.debug("SQL query: {}", sql);
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        List<Map<String, String>> tableData = new ArrayList<>();
        try {
            conn = DriverManager.getConnection(url, user, pwd);
            st = conn.prepareStatement(sql);
            st.setMaxRows(MAX_ROWS);
            rs = st.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while (rs.next()) {
                Map<String, String> rowData = new HashMap<>();
                for (int i = 1; i <= columns; i++) {
                    Object value = rs.getObject(i);
                    rowData.put(md.getColumnLabel(i), value != null ? value.toString() : null);
                }
                tableData.add(rowData);
            }
            return tableData;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
                log.debug("SQL result: {}", tableData);
                log.debug("-----------------------");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int executeUpdate(String sql) {
        log.debug("---- SQL UPDATE REQUEST ----");
        log.debug("Driver: {}", driverClassName);
        log.debug("Database url: {}", url);
        log.debug("SQL update: {}", sql);
        Connection conn = null;
        Statement st = null;
        int affected = 0;
        try {
            conn = DriverManager.getConnection(url, user, pwd);
            st = conn.prepareStatement(sql);
            affected = st.executeUpdate(sql);
            return affected;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
                log.debug("SQL affected rows: {}", affected);
                log.debug("-----------------------");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Builder {
        private String url;
        private String user;
        private String pwd;
        private String driver = "com.mysql.jdbc.Driver";

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder pwd(String pwd) {
            this.pwd = pwd;
            return this;
        }

        public Builder driver(String driver) {
            this.driver = driver;
            return this;
        }

        public SqlClient build() {
            return new SqlClient(this);
        }
    }
}

