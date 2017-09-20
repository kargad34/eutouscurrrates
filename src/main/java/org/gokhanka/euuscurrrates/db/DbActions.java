package org.gokhanka.euuscurrrates.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gokhanka.euuscurrrates.utility.Utility;

/**
 * the queries on the db ,  caching object for the latest rate is managed here
 * @author gokhanka
 *
 */
public class DbActions {

    private static String       SQL_GET_HISTORY   = "SELECT donedate, rate FROM eurtousd WHERE donedate between ? AND  ?  ORDER BY donedate  DESC";
    private static String       SQL_INSERT_LATEST = "INSERT INTO EURTOUSD VALUES (?, ?)";
    private static String       SQL_GET_LATEST    = "SELECT donedate, rate FROM eurtousd ORDER BY donedate  DESC FETCH FIRST ROW ONLY";
    private static String       SQL_GET_COUNT     = "SELECT COUNT(donedate)  rate FROM eurtousd";
    private static final Logger logger            = LogManager.getLogger();
    private static EurToUsd     latest            = null;
    private static Object       mutex             = new Object();

    /**
     * To Set the latest rate in order to cache it for the upcoming
     * request for same data
     * @param EurToUsd lat
     */
    private static void setLatest(EurToUsd lat) {
        synchronized (mutex) {
            latest = lat;
        }
    }

    /**
     * Same as setLatest but for only null cases
     * @param lat
     */
    private static void setLatestIfNull(EurToUsd lat) {
        synchronized (mutex) {
            if (latest == null)
                latest = lat;
        }
    }

    private static EurToUsd getLatest() {
        EurToUsd result = null;
        synchronized (mutex) {
            result = latest;
        }
        return result;
    }

    /**
     * to get the rated between two dates is handled here directly on DB
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<EurToUsd> getHistoricRates(int startDate, int endDate) {
        long methodStartTime = System.currentTimeMillis();
        long elapsedTime = 0;
        List<EurToUsd> response = new ArrayList<EurToUsd>();

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        try {
            conn = DbConnectionManager.getInstance().getConnection();

            pStmt = conn.prepareStatement(SQL_GET_HISTORY);
            pStmt.setInt(1, startDate);
            pStmt.setInt(2, endDate);
            rs = pStmt.executeQuery();

            while (rs != null && rs.next()) {
                EurToUsd euToUs = new EurToUsd();
                euToUs.setDoneDate(rs.getInt(1));
                euToUs.setRate(rs.getString(2));
                response.add(euToUs);
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("getHistoricRates Exception occured", e);
        } catch (Throwable t) {
            logger.error("getHistoricRates Exception Throwable occurred", t);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error("rs close problem!", e);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (SQLException e) {
                    logger.error("pStmt close problem!", e);
                }
            }
            if (conn != null) {
                DbConnectionManager.getInstance().closeConnection(conn);
            }
        }
        elapsedTime = System.currentTimeMillis() - methodStartTime;
        logger.info(" getHistoricRates resonponded with list size: " + response.size()
                + " &elapsedTime: " + elapsedTime);
        return response;
    }

    /**
     * to insert the latest rate value gathered from Rest service or the mock service by the scheduler
     * @param doneDate
     * @param rate
     * @return
     */
    public static boolean insertLates(int doneDate, String rate) {
        long methodStartTime = System.currentTimeMillis();
        long elapsedTime = 0;
        boolean response = Utility.TRUE;

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        try {
            conn = DbConnectionManager.getInstance().getConnection();
            pStmt = conn.prepareStatement(SQL_INSERT_LATEST);
            pStmt.setInt(1, doneDate);
            pStmt.setString(2, rate);
            pStmt.executeUpdate();
            conn.commit();
            EurToUsd tmp = new EurToUsd();
            tmp.setDoneDate(doneDate);
            tmp.setRate(rate);
            setLatest(tmp);
        } catch (Exception e) {
            response = Utility.FALSE;
            logger.error("insertLates Exception occured", e);
        } catch (Throwable t) {
            response = Utility.FALSE;
            logger.error("insertLates Exception Throwable occurred", t);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error("rs close problem!", e);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (SQLException e) {
                    logger.error("pStmt close problem!", e);
                }
            }
            if (conn != null) {
                DbConnectionManager.getInstance().closeConnection(conn);
            }
        }
        elapsedTime = System.currentTimeMillis() - methodStartTime;
        logger.info(" insertLates resonponded with response: " + response + " &elapsedTime: "
                + elapsedTime);
        return response;
    }

    /**
     * to delete a record
     * @param doneDate
     * @return
     */
    public static boolean deleteItem(int doneDate) {
        long methodStartTime = System.currentTimeMillis();
        long elapsedTime = 0;
        boolean response = Utility.TRUE;

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        try {
            conn = DbConnectionManager.getInstance().getConnection();
            pStmt = conn.prepareStatement("DELETE FROM EURTOUSD WHERE DONEDATE=?");
            pStmt.setInt(1, doneDate);
            pStmt.executeUpdate();
            conn.commit();
        } catch (Exception e) {
            response = Utility.FALSE;
            logger.error("delete Exception occured", e);
        } catch (Throwable t) {
            response = Utility.FALSE;
            logger.error("delete Exception Throwable occurred", t);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error("rs close problem!", e);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (SQLException e) {
                    logger.error("pStmt close problem!", e);
                }
            }
            if (conn != null) {
                DbConnectionManager.getInstance().closeConnection(conn);
            }
        }
        elapsedTime = System.currentTimeMillis() - methodStartTime;
        logger.info(" delete resonponded with response: " + response + " &elapsedTime: "
                + elapsedTime);
        return response;
    }

    /**
     * DB interface to get latest rate
     * if the cached value is null data is retrieved from DB and 
     * the cached value is updated if still it is null
     * @return EurToUsd
     */
    public static EurToUsd getLatestRate() {
        long methodStartTime = System.currentTimeMillis();
        long elapsedTime = 0;
        EurToUsd response = null;
        if (getLatest() == null) {
            Connection conn = null;
            ResultSet rs = null;
            PreparedStatement pStmt = null;
            try {
                conn = DbConnectionManager.getInstance().getConnection();
                pStmt = conn.prepareStatement(SQL_GET_LATEST);
                rs = pStmt.executeQuery();
                while (rs != null && rs.next()) {
                    EurToUsd euToUs = new EurToUsd();
                    euToUs.setDoneDate(rs.getInt(1));
                    euToUs.setRate(rs.getString(2));
                    response = euToUs;
                    setLatestIfNull(response);
                }
                conn.commit();
            } catch (Exception e) {
                logger.error("getLatestRate Exception occured", e);
            } catch (Throwable t) {
                logger.error("getLatestRate Exception Throwable occurred", t);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        logger.error("rs close problem!", e);
                    }
                }
                if (pStmt != null) {
                    try {
                        pStmt.close();
                    } catch (SQLException e) {
                        logger.error("pStmt close problem!", e);
                    }
                }
                if (conn != null) {
                    DbConnectionManager.getInstance().closeConnection(conn);
                }
            }
        } else {
            response = getLatest();
        }

        elapsedTime = System.currentTimeMillis() - methodStartTime;
        if (response != null)
            logger.info(" getLatestRate resonponded with rate: " + response.getRate()
                    + " &elapsedTime: " + elapsedTime);
        else
            logger.error(" getLatestRate resonponded with null: " + " elapsedtime" + elapsedTime);
        return response;
    }

    /**
     * DB interface to get count of rows in the table
     * @return int
     */
    public static int getCountOfRecords() {
        long methodStartTime = System.currentTimeMillis();
        long elapsedTime = 0;
        int response = 0;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pStmt = null;
        try {
            conn = DbConnectionManager.getInstance().getConnection();
            pStmt = conn.prepareStatement(SQL_GET_COUNT);
            rs = pStmt.executeQuery();
            while (rs != null && rs.next()) {
                response = rs.getInt(1);
            }
            conn.commit();
        } catch (Exception e) {
            logger.error("getCountOfRecords Exception occured", e);
        } catch (Throwable t) {
            logger.error("getCountOfRecords Exception Throwable occurred", t);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error("rs close problem!", e);
                }
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                } catch (SQLException e) {
                    logger.error("pStmt close problem!", e);
                }
            }
            if (conn != null) {
                DbConnectionManager.getInstance().closeConnection(conn);
            }
        }
        elapsedTime = System.currentTimeMillis() - methodStartTime;
        logger.info(" getCountOfRecords resonponded with count: " + response + " &elapsedTime: "
                + elapsedTime);

        return response;
    }
}
