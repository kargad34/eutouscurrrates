package org.gokhanka.euuscurrrates.rates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import com.google.gson.Gson;
import java.net.URL;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gokhanka.euuscurrrates.utility.Utility;
/**
 *  Usd Rates are retrieved by http interface
 *  http://api.fixer.io/latest?symbols=USD,EUR
 *  
 * @author gokhanka
 *
 */
public class UsdRateGetter {

    private static final Logger  logger         = LogManager.getLogger();
    private static Object        mutex          = new Object();
    private static UsdRateGetter instance       = null;
    static final String          encoding       = "UTF-8";
    private String               urlDest        = null;
    private long                 lastCalledTime = 0L;

    private UsdRateGetter() {
        super();
        this.urlDest = "http://api.fixer.io/latest?symbols=USD,EUR";
    }

    public static UsdRateGetter getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null)
                    instance = new UsdRateGetter();
            }
        }
        return instance;
    }

    public String getRate() {
        String result = null;
        if (System.currentTimeMillis() - lastCalledTime > Utility.MIN_REFRESH_PERIOD) {
            lastCalledTime = System.currentTimeMillis();
            HttpURLConnection hCon = getConnection(urlDest);
            boolean useMocService = Utility.FALSE;
            if (hCon != null) {
                BaseCurr bCur = parseDoc(hCon);
                if (bCur != null) {
                    result = bCur.getRates().getUSD();
                } else
                    useMocService = Utility.TRUE;
            } else
                useMocService = Utility.TRUE;
            if (useMocService)
                result = rateMockService();
        } else
            result = rateMockService();
        return result;
    }

    /**
    * as the http service provider updates the data only once a day
    * i decided to mock the service by a simple method which
    * generates a rate randomly
    * @return
    */
    private String rateMockService() {
        Random rndm = new Random(System.currentTimeMillis());
        return Float.toString(1 + rndm.nextFloat()).substring(0, 6);
    }

    /**
     * to parse the result of http response the method is used
     * As the response is in json format google gson api is used
     * @param conn
     * @return
     */
    public static BaseCurr parseDoc(HttpURLConnection conn) {
        BufferedReader br = null;
        BaseCurr base = null;
        Gson gson = new Gson();
        try {
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            String jsonData = "";
            while ((output = br.readLine()) != null) {
                jsonData += output + "\n";
            }
            base = gson.fromJson(jsonData, BaseCurr.class);
        } catch (IOException e) {
            logger.error("Error in connecting to url IOException", e);
        } catch (Exception e) {
            logger.error("Error in connecting to url IOException", e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
                logger.error("Error in connecting to url IOException", ex);
            } catch (Exception e) {
                logger.error("Error in connecting to url Exception", e);
            }
        }
        conn.disconnect();
        return base;
    }

    /**
    * to get connection to the url this method is used
    * @param urlName
    * @return
    */
    public static HttpURLConnection getConnection(String urlName) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlName);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                conn = null;
            }
        } catch (MalformedURLException e) {
            logger.error("Error in connecting to url MalformedURLException", e);
        } catch (ProtocolException e) {
            logger.error("Error in connecting to url ProtocolException", e);
        } catch (IOException e) {
            logger.error("Error in connecting to url IOException", e);
        } catch (Exception e) {
            logger.error("Error in connecting to url Exception", e);
        }
        return conn;
    }
}

/**
 * for gson, mapping the json result to java object
 */
class BaseCurr {

    private String            base;
    private String            date;
    private CorrespondingCurr rates;

    public String getBase() {
        return base;
    }
    public void setBase(String base) {
        this.base = base;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public CorrespondingCurr getRates() {
        return rates;
    }
    public void setRates(CorrespondingCurr rates) {
        this.rates = rates;
    }
    @Override
    public String toString() {
        return "base: " + this.getBase() + " date:" + this.getDate() + " rates: "
                + this.rates.toString();
    }
}
/**
 * for gson, mapping the json result to java object
 */
class CorrespondingCurr {

    private String USD;

    public String getUSD() {
        return USD;
    }
    public void setUSD(String uSD) {
        USD = uSD;
    }
    @Override
    public String toString() {
        return " USD: " + this.getUSD();
    }
}