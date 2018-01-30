package aayush.khanna.com.stocksearcher_droidapp;


import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;

public class CustomStockListAdapter extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] itemKey;
    private final String[] itemValue;
    private final Integer[] imgid;

    public CustomStockListAdapter(Activity context, String[] itemKey, String[] itemValue, Integer[] imgid) {
        super(context, R.layout.list_stock_details, itemKey);

        this.context = context;
        this.itemKey = itemKey;
        this.itemValue = itemValue;
        this.imgid = imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_stock_details, null,true);

        TextView textKey = (TextView) rowView.findViewById(R.id.stock_row_key);
        TextView textValue = (TextView) rowView.findViewById(R.id.stock_row_value);

        textKey.setText(itemKey[position]);
        textValue.setText(itemValue[position]);

        if(position == 2) {
            Log.d("itemValue", "change: " + itemValue[position]);
            String result = itemValue[position].split(" ")[0];
            float check = Float.parseFloat(result);
            if(check > 0) {
                ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
                imageView.setImageResource(imgid[1]);
            }
            else if(check < 0) {
                ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
                imageView.setImageResource(imgid[0]);
            }
            else {

            }
            Log.d("StockAdapter", "CustomStockListAdapter -> getView: " + position);
        }

        return rowView;

    };
}
