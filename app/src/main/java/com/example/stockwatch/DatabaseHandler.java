package com.example.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";

    private static final String DATABASE_NAME = "StockWatchDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "StocksTable";
    private static final String SYMBOL = "symbol";
    private static final String COMPANY_NAME = "name";


    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    SYMBOL + " TEXT not null unique," +
                    COMPANY_NAME + " TEXT not null);";

    private SQLiteDatabase database;

    DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
        if (database == null) {
            Log.d(TAG, "DatabaseHandler: DATABASE IS NULL");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    public ArrayList<Stock> getStocks() {
        ArrayList<Stock> stockList = new ArrayList<>();

        Cursor cursor = database.query(
                TABLE_NAME,
                new String[]{SYMBOL, COMPANY_NAME},
                null,
                null,
                null,
                null,
                SYMBOL);

        if(cursor != null) {
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                Stock stock = new Stock(cursor.getString(0), cursor.getString(1));
                stockList.add(stock);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return stockList;
    }

    public boolean insertStock(String symbol, String companyName) {
        ContentValues values = new ContentValues();

        values.put(SYMBOL, symbol);
        values.put(COMPANY_NAME, companyName);

        long numRows = database.insert(TABLE_NAME, null, values);
        return numRows != -1;
    }

    public void deleteStockBySymbol(String symbol) {
        int numRows = database.delete(TABLE_NAME, SYMBOL + " = ?", new String[]{symbol});
        Log.d(TAG, "deleteStockBySymbol: " + numRows);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    void dumpDbToLog() {
        Cursor cursor = database.rawQuery("select * from " + TABLE_NAME, null);
        if (cursor != null) {
            cursor.moveToFirst();

            Log.d(TAG, "dumpDbToLog: vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");
            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String companyName = cursor.getString(1);
                Log.d(TAG, "dumpDbToLog: " +
                        String.format("%s %-18s", SYMBOL + ":", symbol) +
                        String.format("%s %-18s", COMPANY_NAME + ":", companyName));
                cursor.moveToNext();
            }
            cursor.close();
        }

        Log.d(TAG, "dumpDbToLog: ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
    }
}
