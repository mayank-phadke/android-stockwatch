package com.example.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StocksAdapter extends RecyclerView.Adapter<StocksViewHolder> {

    private List<Stock> stockList;
    private MainActivity mainActivity;

    public StocksAdapter(List<Stock> stockList, MainActivity mainActivity) {
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StocksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_row, parent, false);

        view.setOnClickListener(mainActivity);
        view.setOnLongClickListener(mainActivity);

        return new StocksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StocksViewHolder holder, int position) {
        Stock stock = stockList.get(position);

        holder.stockSymbol.setText(stock.getStockSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.latestPrice.setText(String.format(Locale.US, "%.2f", stock.getPrice()));
        String price_change =
                String.format(Locale.US, "%.2f", stock.getPriceChange())
                        + "("
                        + String.format(Locale.US, "%.2f", stock.getChangePercentage())
                        + ")";
        holder.priceChange.setText(price_change);
        if(stock.getPriceChange() >= 0) {
            setPositiveHolder(holder);
        } else {
            setNegativeHolder(holder);
        }
    }

    private void setPositiveHolder(@NonNull StocksViewHolder holder) {
        holder.changeImage.setImageDrawable(ContextCompat.getDrawable(mainActivity, R.drawable.ic_up_arrow));
        holder.stockSymbol.setTextColor(Color.GREEN);
        holder.companyName.setTextColor(Color.GREEN);
        holder.latestPrice.setTextColor(Color.GREEN);
        holder.priceChange.setTextColor(Color.GREEN);
    }

    private void setNegativeHolder(@NonNull StocksViewHolder holder) {
        holder.changeImage.setImageDrawable(ContextCompat.getDrawable(mainActivity, R.drawable.ic_down_arrow));
        holder.stockSymbol.setTextColor(Color.RED);
        holder.companyName.setTextColor(Color.RED);
        holder.latestPrice.setTextColor(Color.RED);
        holder.priceChange.setTextColor(Color.RED);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
