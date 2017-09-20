package org.gokhanka.euuscurrrates.utility;

import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import com.google.gson.Gson;
import spark.ResponseTransformer;

/**
 * Utility class for the project
 * 
 * Holding all parametric and fixed values used in the project
 * if dynamic configuration is needed these parameters can be used
 * 
 * Plus: utility methods are here
 * @author gokhanka
 *
 */
public class Utility {

    public static String           dateFormat                   = "yyMMddHHmm";
    public static String           success                      = "SUCCESS";
    public static String           EMPTY_RESULT                 = "EMPTY RESULT SET";
    public static String           TPS_EXCEEDED                 = "TPS Exceeded";
    public static String           INTERNAL_ERROR               = "Internal Error";
    public static String           TIMEOUT_ERR                  = "Process Took Longer Time Then Expected";
    public static String           WRONG_INPUT                  = "start and end dates should be in yyMMddHHmm format & startDate should be greater than endDate";
    public static int              SCHEDULAR_INIT_DELAY         = 10;
    public static int              SCHEDULAR_REG_DELAY          = 60;
    public static long             MIN_REFRESH_PERIOD           = 120 * 60 * 1000;
    public static SimpleDateFormat df                           = new SimpleDateFormat(dateFormat);
    public static int              NEGATIVE_ONE                 = -1;
    public static int              ZERO                         = 0;
    public static int              ONE                          = 1;
    public static int              TWO                          = 2;
    public static int              THREE                          = 3;
    public static String           DELIMETER                    = " ";
    public static int              HTTP_PORT                    = 8088;
    public static int              HTTP_THEAD_MAX               = 100;
    public static int              HTTP_THEAD_MIN               = 5;
    public static int              HTTP_IDLE_TIME               = 3000;
    public static int              MAX_ALLOWED_REQUEST          = 100;
    public static int              MAX_ALLOWED_REQUEST_DURATION = 1000;
    public static long             RESPONSE_TIMEOUT             = 2000;
    public static String           START                        = "start";
    public static String           STOP                         = "stop";
    public static String           BLOCK                        = "block";
    public static boolean          TRUE                         = true;
    public static boolean          FALSE                        = false;
    public static String           propertyFileName             = "src/main/resources/eurotousd.properties";
    private static final Logger    logger                       = LogManager.getLogger();

    public static int[] validateInput(String start, String end) {
        int[] result = null;
        if (start == null || end == null || start.length() != 10 || end.length() != 10
                || start.equalsIgnoreCase(end))
            result = null;
        ;
        try {
            Date startDate = df.parse(start);
            Date endDate = df.parse(end);
            result = new int[2];
            result[0] = Integer.parseInt(df.format(startDate));
            result[1] = Integer.parseInt(df.format(endDate));
            if (result[1] <= result[0])
                result = null;
        } catch (NumberFormatException e) {
            result = null;
        } catch (ParseException e) {
            result = null;
        }
        return result;
    }

    public static void quit() {
        logger.error("Goodbye in a couple of seconds ...");       
        //System.exit(0);
    }

    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static ResponseTransformer json() {
        return Utility::toJson;
    }

    public static void loadProperties() {
        Properties moduleProps = null;
        moduleProps = new Properties();
        try {
            FileInputStream fis = new FileInputStream(propertyFileName);
            moduleProps.load(fis);
        } catch (FileNotFoundException e) {
            logger.error(propertyFileName + " not found!", e);
            logger.info("<Starter> Default properties will be used...");
            return;
        } catch (IOException e) {
            logger.error("<Starter> IO Exception while reading " + propertyFileName, e);
            logger.info("<Starter> Default properties will be used...");
            return;
        }
        try {
            HTTP_PORT = Integer.parseInt(moduleProps.getProperty("HTTP_PORT").trim());
            HTTP_THEAD_MAX = Integer.parseInt(moduleProps.getProperty("HTTP_THEAD_MAX").trim());
            HTTP_THEAD_MIN = Integer.parseInt(moduleProps.getProperty("HTTP_THEAD_MIN").trim());
            HTTP_IDLE_TIME = Integer.parseInt(moduleProps.getProperty("HTTP_IDLE_TIME").trim());
            MAX_ALLOWED_REQUEST = Integer.parseInt(moduleProps.getProperty("MAX_ALLOWED_REQUEST").trim());
            MAX_ALLOWED_REQUEST_DURATION = Integer.parseInt(moduleProps.getProperty("MAX_ALLOWED_REQUEST_DURATION").trim());
            success = moduleProps.getProperty("SUCCESS").trim();
            EMPTY_RESULT = moduleProps.getProperty("EMPTY_RESULT").trim();
            TPS_EXCEEDED = moduleProps.getProperty("TPS_EXCEEDED").trim();
            INTERNAL_ERROR = moduleProps.getProperty("INTERNAL_ERROR").trim();
            TIMEOUT_ERR = moduleProps.getProperty("TIMEOUT_ERR").trim();
            WRONG_INPUT = moduleProps.getProperty("WRONG_INPUT").trim();
            SCHEDULAR_INIT_DELAY = Integer.parseInt(moduleProps.getProperty("SCHEDULAR_INIT_DELAY").trim());
            SCHEDULAR_REG_DELAY = Integer.parseInt(moduleProps.getProperty("SCHEDULAR_REG_DELAY").trim());
            MIN_REFRESH_PERIOD = (long) ((long) Integer.parseInt(moduleProps.getProperty("MIN_REFRESH_PERIOD").trim())
                    * (long) (60 * 1000));
            RESPONSE_TIMEOUT = Long.parseLong(moduleProps.getProperty("RESPONSE_TIMEOUT").trim());
        } catch (Exception e) {
            logger.error("<Starter> Exception while getting properties from  " + propertyFileName,
                         e);
            logger.info("<Starter> Default properties will be used...");
        }
    }
}
