package org.gokhanka.euuscurrrates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.gokhanka.euuscurrrates.api.ApiResult;

import com.google.gson.Gson;

public class TestUtility {
    public static ApiResult parseDoc(HttpURLConnection conn) {
        BufferedReader br = null;
        ApiResult base = null;
        Gson gson = new Gson();
        try {
            br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            String jsonData = "";
            while ((output = br.readLine()) != null) {
                jsonData += output + "\n";
            }
            base = gson.fromJson(jsonData, ApiResult.class);
        } catch (IOException e) {
        } catch (Exception e) {
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException ex) {
            } catch (Exception e) {
            }
        }
        conn.disconnect();
        return base;
    }

    public static HttpURLConnection getConnection(String name) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(name);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                conn = null;
            }
        } catch (MalformedURLException e) {
        } catch (ProtocolException e) {
        } catch (IOException e) {
        } catch (Exception e) {
        }
        return conn;
    }
}
