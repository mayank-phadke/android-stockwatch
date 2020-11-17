package com.example.stockwatch;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StocksViewHolder extends RecyclerView.ViewHolder {

    TextView stockSymbol, companyName, latestPrice, priceChange;
    ImageView changeImage;

    public StocksViewHolder(@NonNull View itemView) {
        super(itemView);
        stockSymbol = itemView.findViewById(R.id.stock_symbol_text_view);
        companyName = itemView.findViewById(R.id.company_name_text_view);
        latestPrice = itemView.findViewById(R.id.latest_price_text_view);
        priceChange = itemView.findViewById(R.id.price_change_text_view);
        changeImage = itemView.findViewById(R.id.change_image);
    }
}
