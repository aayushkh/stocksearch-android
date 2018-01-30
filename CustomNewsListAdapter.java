package aayush.khanna.com.stocksearcher_droidapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomNewsListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> itemTitles;
    private final ArrayList<String> itemPubDates;
    private final ArrayList<String> itemAuthors;

    public CustomNewsListAdapter(@NonNull Activity context, ArrayList<String> itemTitles, ArrayList<String> itemPubDates, ArrayList<String> itemAuthors) {
        super(context, R.layout.list_stock_details, itemTitles);

        this.context = context;
        this.itemTitles = itemTitles;
        this.itemPubDates = itemPubDates;
        this.itemAuthors = itemAuthors;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_news_details, null, true);

        TextView textTitles = (TextView) rowView.findViewById(R.id.news_header);
        TextView textPubDates = (TextView) rowView.findViewById(R.id.news_date);
        TextView textAuthors = (TextView) rowView.findViewById(R.id.news_author);

        textTitles.setText(itemTitles.get(position));
        textPubDates.setText(itemPubDates.get(position));
        textAuthors.setText(itemAuthors.get(position));

        return rowView;
    }
}
