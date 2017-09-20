package org.gokhanka.euuscurrrates.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gokhanka.euuscurrrates.utility.Utility;

/**
 * to manage DB it self and the connections
 * @author gokhanka
 *
 */
public class DbConnectionManager {
    
    static DbConnectionManager instance = null;
    static Object mutex = new Object();
    static String framework = "embedded";
    static String protocol = "jdbc:derby:";
    static String dbName = "derbyDB";
    BasicDataSource ds;
    static final Logger    logger                       = LogManager.getLogger();
    /**
     *  apache derby is used in embedded and network mode to store the data
     *  for the connection pooling apache dbcp2 is used 
     *  both tools are used in their simplest configuration
     *  The pool values and the db connection parameters can be in configuration
     *  but I left them in code, as hard coded.
     */
    private DbConnectionManager() {
        ds = new BasicDataSource();
        ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        ds.setUsername("user1");
        ds.setPassword("user1");
        ds.setUrl("jdbc:derby:derbyDB;create=true");

        ds.setMinIdle(5);
        ds.setMaxIdle(20);
        ds.setMaxTotal(100);
        ds.setMaxWaitMillis(2000);
        ds.setMaxOpenPreparedStatements(180);
        ds.setDefaultAutoCommit(Utility.TRUE);
    }
    /**
     * create the table that we will work on, if not exist
     */
    private void createTable() {
        Connection conn = null;
        Statement s = null;
        try {
            conn = getConnection();
            logger.info("Connected to and created database " + dbName);
            DatabaseMetaData dbmd = conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, "USER1", "EURTOUSD", null);
            if (!rs.next()) {
                s = conn.createStatement();
                s.execute("CREATE TABLE EURTOUSD (DONEDATE int NOT NULL, RATE varchar(10), PRIMARY KEY (DONEDATE))");
                logger.info("Created table EURTOUSD");
            } else {
                logger.info("Table EURTOUSD Already Exists");
            }
        } catch (Exception e) {
            logger.error("Error in connecting to db",e);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {

                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {

                }
            }
        }
    }
    public static DbConnectionManager getInstance(){
        if(instance==null){
            synchronized (mutex){
                if (instance == null) {
                    instance = new DbConnectionManager();
                    instance.createTable();
                }
            }
        }
        return instance;
    }
    public Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }
    
    public void closeConnection(Connection c) {
        try {
            if (c != null && !c.isClosed())
                c.close();
        } catch (Exception e) {
            
        }
    }
    public void closeDB() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException se) {
            if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState())))) {
                logger.info("Derby shut down normally");
                // Note that for single database shutdown, the expected
                // SQL state is "08006", and the error code is 45000.
            } else {
                // if the error code or SQLState is different, we have
                // an unexpected exception (shutdown failed)
                logger.info("Derby did not shut down normally");
            }
        }
    }
}
