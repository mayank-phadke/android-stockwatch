package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NameDownloader implements Runnable {

    private static final String TAG = "NameDownloader";
    private static final String DATA_URI = "https://api.iextrading.com/1.0/ref-data/symbols";
    private MainActivity mainActivity;
    private String pattern;

    public NameDownloader(String pattern, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.pattern = pattern;
    }

    @Override
    public void run() {
        Uri dataUri = Uri.parse(DATA_URI);
        String urlToUse = dataUri.toString();

        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP Response Code Not OK");
                handleResults(null);
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            HashMap<String, String> stockHashMap = parseAndFilterJSON(sb.toString(), pattern);
            handleResults(stockHashMap);

            Log.d(TAG, "run: " + sb.toString());
        } catch (Exception e) {
            Log.d(TAG, "run: " + e.getMessage());
            handleResults(null);
        }
    }

    private HashMap<String, String> parseAndFilterJSON(String jsonString, String pattern) {
        HashMap<String, String> stockHashMap = new HashMap<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);

            String lowerCasePattern = pattern.toLowerCase();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.getString("symbol").toLowerCase().contains(lowerCasePattern) ||
                        jsonObject.getString("name").toLowerCase().contains(lowerCasePattern)) {
                        String symbol = jsonObject.getString("symbol");
                        String name = jsonObject.getString("name");

                        stockHashMap.put(symbol, name);
                }
            }
            
        } catch (Exception e) {
            Log.d(TAG, "parseAndFilterJSON: Error parsing JSON");
        }

        return stockHashMap;
    }

    private void handleResults(final HashMap<String, String> stockHashMap) {
        if (stockHashMap == null) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.nameDownloadFailed();
                }
            });
            return;
        }

        if (stockHashMap.size() == 0) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.showSymbolNotFoundDialog(pattern);
                }
            });
            return;
        }

        if (stockHashMap.size() == 1) {
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Map.Entry<String, String> entry = stockHashMap.entrySet().iterator().next();
                    mainActivity.addNewStock(entry.getKey(), entry.getValue());
                }
            });
            return;
        }

        stockHashMap.size();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.showStockListDialog(stockHashMap);
            }
        });
    }
}
