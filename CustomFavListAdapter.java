package aayush.khanna.com.stocksearcher_droidapp;


import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomFavListAdapter extends ArrayAdapter<String>{

    private final Activity context;
    private final ArrayList<String> itemSymbol;
    private final ArrayList<String> itemPrice;
    private final ArrayList<String> itemChange;
    private final ArrayList<String> itemChangePercent;

    public CustomFavListAdapter(@NonNull Activity context, ArrayList<String> itemSymbol, ArrayList<String> itemPrice, ArrayList<String> itemChange, ArrayList<String> itemChangePercent) {
        super(context, R.layout.list_fav_stock, itemSymbol);

        this.context = context;
        this.itemSymbol = itemSymbol;
        this.itemPrice = itemPrice;
        this.itemChange = itemChange;
        this.itemChangePercent = itemChangePercent;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_fav_stock, null, true);

        TextView textTitles = (TextView) rowView.findViewById(R.id.stock_name);
        TextView textPubDates = (TextView) rowView.findViewById(R.id.stock_price);
        TextView textAuthors = (TextView) rowView.findViewById(R.id.stock_change);
        String change = itemChange.get(position) + " (" + itemChangePercent.get(position) + "%)";

        textTitles.setText(itemSymbol.get(position));
        textPubDates.setText(itemPrice.get(position));
        textAuthors.setText(change);

        String tempStr = itemChange.get(position);
        float tempVal = Float.parseFloat(tempStr);
        if(tempVal > 0) {
            textAuthors.setTextColor(Color.parseColor("#00FF00"));
        }
        else if(tempVal < 0) {
            textAuthors.setTextColor(Color.parseColor("#FF0000"));
        }
        else {
        }

        return rowView;
    }

}
