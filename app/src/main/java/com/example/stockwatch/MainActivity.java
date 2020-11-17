package com.example.stockwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";

    private static final String MARKET_WATCH_URL = "https://www.marketwatch.com/investing/stock/";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private StocksAdapter adapter;
    private final List<Stock> stockList = new ArrayList<>();

    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHandler = new DatabaseHandler(this);
        databaseHandler.dumpDbToLog();

        swipeRefreshLayout = findViewById(R.id.swiperefresh_view);
        recyclerView = findViewById(R.id.recycler_view);

        adapter = new StocksAdapter(stockList, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadStockData();
            }
        });

        populateListFromDB();
        downloadStockData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_stock) {
            showAddStockDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAddStockDialog() {

        if(!isInternetConnected()) {
            showNoInternetDialog();
            return;
        }

        AlertDialog.Builder builder =new AlertDialog.Builder(this);

        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        InputFilter[] inputFilters =  et.getFilters();
        InputFilter[] newInputFilters = new InputFilter[inputFilters.length + 1];
        System.arraycopy(inputFilters, 0, newInputFilters, 0, inputFilters.length);
        newInputFilters[inputFilters.length] = new InputFilter.AllCaps();
        et.setFilters(newInputFilters);
        et.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(et);

        builder
                .setTitle("Stock Selection")
                .setMessage("Please enter a Stock Symbol")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new Thread(new NameDownloader(et.getText().toString(), MainActivity.this)).start();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        builder.create().show();
    }

    private void downloadStockData() {

        if(!isInternetConnected()) {
            showNoInternetDialog();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        new Thread(new StockDataDownloader(this, stockList)).start();
    }

    public void updateStockList(List<Stock> stockList) {
        List<Stock> newList = new ArrayList<>(stockList);
        this.stockList.clear();
        this.stockList.addAll(newList);
        adapter.notifyDataSetChanged();

        if(swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("No Network Connection")
                .setMessage("Stocks Cannot Be Added Or Updated Without A Network Connection");

        builder.create().show();
    }

    public void showSymbolNotFoundDialog(String pattern) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Stock Symbol Not Found: " + pattern)
                .setMessage("Data for stock symbol");

        builder.create().show();
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isInternetConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.e(TAG, "isInternetConnected: Cannot access Connectivity Manager");
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

    public void addNewStock(String symbol, String name) {
        boolean stockAdded = databaseHandler.insertStock(symbol, name);
        if(!stockAdded) {
            showDuplicateStockDialog(symbol);
            return;
        }
        populateListFromDB();
        downloadStockData();
    }

    public void removeStock(Stock stock, int index) {
        databaseHandler.deleteStockBySymbol(stock.getStockSymbol());
        stockList.remove(index);
        updateStockList(stockList);
    }

    public void showDuplicateStockDialog(String symbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning))
                .setTitle("Duplicate Stock")
                .setMessage("Stock Symbol " + symbol + " is already displayed");
        builder.create().show();
    }

    public void showStockListDialog(final HashMap<String, String> stockHashMap) {

        List<String> sList = new ArrayList<>();
        final List<String> keyList = new ArrayList<>();
        for (Map.Entry<String, String> entry : stockHashMap.entrySet()) {
            sList.add(entry.getKey() + " - " + entry.getValue());
            keyList.add(entry.getKey());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        builder
                .setItems(sList.toArray(new CharSequence[sList.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String key = keyList.get(i);
                        String value = stockHashMap.get(key);
                        addNewStock(key, value);
                    }
                })
                .setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });

        builder.create().show();
    }

    private void populateListFromDB() {
        ArrayList<Stock> newList = databaseHandler.getStocks();

        updateStockList(newList);
    }

    @Override
    public void onClick(View view) {
        int index = recyclerView.getChildAdapterPosition(view);
        Stock stock = stockList.get(index);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_WATCH_URL + stock.getStockSymbol()));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String symbol = ((TextView)view.findViewById(R.id.stock_symbol_text_view)).getText().toString();
        final int index = recyclerView.getChildAdapterPosition(view);
        builder
                .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_delete))
                .setTitle("Delete Stock")
                .setMessage("Delete Stock Symbol " + symbol + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Stock stock = stockList.get(index);
                        removeStock(stock, index);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });

        builder.create().show();
        return true;
    }

    public void nameDownloadFailed() {
        showNoInternetDialog();
    }
}