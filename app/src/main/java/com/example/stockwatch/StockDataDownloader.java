package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StockDataDownloader implements Runnable {

    private static final String TAG = "StockDataDownloader";

    private static final String API_KEY = "YOUR_API_KEY";
    private static final String DATA_URI = "https://cloud.iexapis.com/stable/stock";

    private MainActivity mainActivity;
    private List<Stock> stockList;

    public StockDataDownloader(MainActivity mainActivity, List<Stock> stockList) {
        this.mainActivity = mainActivity;
        this.stockList = stockList;
    }

    @Override
    public void run() {
        Log.d(TAG, "run: Start Stock Data Download");
        final List<Stock> newList = new ArrayList<>(stockList);
        try {
            for (Stock stock : newList) {
                String uri = DATA_URI + "/" + stock.getStockSymbol() + "/quote?token=" + API_KEY;
                Uri dataUri = Uri.parse(uri);
                String urlToUse = dataUri.toString();
                URL url = new URL(urlToUse);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "run: HTTP response code not OK");
                    continue;
                }

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                JSONObject stockJObj = new JSONObject(sb.toString());
                Log.d(TAG, "run: JSON Object" + sb);
                if (stockJObj.isNull("latestPrice")) {
                    stock.setPrice(0);
                } else {
                    stock.setPrice(stockJObj.getDouble("latestPrice"));
                }

                if (stockJObj.isNull("change")) {
                    stock.setPriceChange(0);
                } else {
                    stock.setPriceChange(stockJObj.getDouble("change"));
                }

                if (stockJObj.isNull("changePercent")) {
                    stock.setChangePercentage(0);
                } else {
                    stock.setChangePercentage(stockJObj.getDouble("changePercent"));
                }
            }
            Log.d(TAG, "run: Finish Stock Data Download");
        } catch (Exception e) {
            Log.d(TAG, "run: Error Downloading Stock Data");
            e.printStackTrace();
        }

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.updateStockList(newList);
            }
        });
    }
}