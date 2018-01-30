package aayush.khanna.com.stocksearcher_droidapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class CurrentFragment extends Fragment {
    private static final String TAG = "CURRENT";
    private ImageView fbimg;
    private ImageView star;
    private Spinner indicatorSpinner;
    private String[] KEYS = {"Stock Symbol", "Last Price", "Change", "Timestamp", "Open", "Close", "Day's Range", "Volume"};
    private ListView stock_list;
    Integer[] IMAGES = {R.drawable.down, R.drawable.up};
    private static String[] values;
    private static String[] sharedValues;
    private WebView webViewIndicator;
    private String symbol;
    private TextView changeView;
    private String prev;
    private String prevCheck;
    private ProgressBar p_bar;
    private TextView errorMsg;
    private int flag;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.current, container, false);
        Bundle extras = getActivity().getIntent().getExtras();
        String stocktickersymbol = extras.getString("SYM_CONSTANT");
        symbol = stocktickersymbol;
        Log.d("CurrentFragment", "CurrentFragment -> stocktickersymbol -> \n " + stocktickersymbol);
        stock_list = (ListView) view.findViewById(R.id.stock_list);
        indicatorSpinner = (Spinner) view.findViewById(R.id.spinner_indicator);
        star = (ImageView) view.findViewById(R.id.star);
        star.setImageResource(R.drawable.empty);
        fbimg = (ImageView) view.findViewById(R.id.fbimg);
        webViewIndicator = (WebView) view.findViewById(R.id.webview_indicators);
        changeView = (TextView) view.findViewById(R.id.change);
        p_bar = (ProgressBar) view.findViewById(R.id.progress_bar);
        errorMsg = (TextView) view.findViewById(R.id.error);
        stock_list.setVisibility(View.GONE);
        p_bar.setVisibility(View.VISIBLE);
        errorMsg.setVisibility(View.GONE);
        prev = "";
        prevCheck = "Price";
        flag = 0;

        SharedPreferences sharedPrefTwo = getActivity().getSharedPreferences("favourites", Context.MODE_PRIVATE);
        Map<String, ?> keys = sharedPrefTwo.getAll();
        int starFlag = 1;
        ArrayList<String> tempArr = new ArrayList<String>();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            if ((entry.getKey()).matches(symbol)) {
                starFlag = 0;
            }
        }

        if (starFlag == 0) {
            star.setImageResource(R.drawable.filled);
        }


        getStockData(stocktickersymbol);


        //SPINNER-INDICATOR
        ArrayAdapter<CharSequence> orderAdapter = ArrayAdapter.createFromResource(getContext(), R.array.Indicator, android.R.layout.simple_spinner_item);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        indicatorSpinner.setAdapter(orderAdapter);


        star.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPrefTwo = getActivity().getSharedPreferences("favourites", Context.MODE_PRIVATE);
                Map<String, ?> keys = sharedPrefTwo.getAll();
                int starFlag = 1;
                ArrayList<String> tempArr = new ArrayList<String>();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    if ((entry.getKey()).matches(symbol)) {
                        starFlag = 0;
                    }
                }
                if (starFlag == 1) {
                    Toast.makeText(getActivity(), "Added to Favourites ! ", Toast.LENGTH_SHORT).show();
                    star.setImageResource(R.drawable.filled);
//                    SharedPreferences sharedPref = getActivity().getSharedPreferences("favourites", Context.MODE_PRIVATE);
                    String tempPref = sharedValues[0] + " " + sharedValues[1] + " " + sharedValues[2] + " " + sharedValues[3];
                    SharedPreferences.Editor editor = sharedPrefTwo.edit();
                    editor.putString(sharedValues[0], tempPref);
//                    Log.d("SHARED", "SHAREDPREFERENCES !!: " + sharedValues[0] + " : " + tempPref);
                    editor.apply();
                } else {
                    Toast.makeText(getActivity(), "Removing from Favorites...", Toast.LENGTH_SHORT).show();
                    star.setImageResource(R.drawable.empty);
                    SharedPreferences.Editor editor = sharedPrefTwo.edit();
                    editor.remove(symbol);
                    editor.apply();
                }

            }
        });

        changeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String indicatorText = indicatorSpinner.getSelectedItem().toString();

                if (prev.matches(indicatorText)) {
                    changeView.setTextColor(getResources().getColor(R.color.colorGrey));
                } else {
                    changeView.setTextColor(getResources().getColor(R.color.colorGrey));


                    if (indicatorText.matches("Price")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/Price.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("SMA")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/SMA.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("EMA")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/EMA.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("CCI")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/CCI.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("ADX")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/ADX.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("RSI")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/RSI.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("STOCH")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/STOCH.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("BBANDS")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/BBANDS.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    } else if (indicatorText.matches("MACD")) {
                        webViewIndicator.getSettings().setJavaScriptEnabled(true);
                        webViewIndicator.loadUrl("file:///android_asset/MACD.html");
                        webViewIndicator.setWebViewClient(new WebViewClient() {
                            public void onPageFinished(WebView view, String url) {
                                webViewIndicator.loadUrl("javascript:getData('" + symbol + "')");
                            }
                        });
                    }
                }
                prev = indicatorText;
            }
        });

        fbimg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Posting to Facebook ! ", Toast.LENGTH_SHORT).show();
            }
        });

        indicatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String indicatorText = indicatorSpinner.getSelectedItem().toString();
                if (flag == 1) {
                    if (prevCheck.matches(indicatorText)) {
                        changeView.setTextColor(getResources().getColor(R.color.colorGrey));
                    } else {
                        changeView.setTextColor(getResources().getColor(R.color.colorBlack));
                    }
                } else {
                    flag = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return view;
    }


    public void getStockData(final String tempStr) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String urlPrice = "https://bootstrap-stocksearch.appspot.com/compact?stocktickersymbol=" + tempStr;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlPrice,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Log.d("AC", "addTextChangedListener -> onTextChanged -> response -> \n " + response);
                        try {
                            JSONObject jsonObj = new JSONObject(response);
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> jsonObj -> \n " + jsonObj);
                            String timeStamp = jsonObj.getJSONObject("Meta Data").getString("3. Last Refreshed");
                            JSONObject timeSeries = jsonObj.getJSONObject("Time Series (Daily)");
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> timeSeries -> \n " + timeSeries);
                            Iterator<String> dates = timeSeries.keys();
                            JSONObject lastDay = timeSeries.getJSONObject(dates.next());
                            JSONObject secondLastDay = timeSeries.getJSONObject(dates.next());
                            String openOne = lastDay.getString("1. open");
                            String highOne = lastDay.getString("2. high");
                            String lowOne = lastDay.getString("3. low");
                            String closeOne = lastDay.getString("4. close");
                            String volumeOne = lastDay.getString("5. volume");
                            String closeTwo = secondLastDay.getString("4. close");
                            float tempChange = (Float.parseFloat(closeOne)) - (Float.parseFloat(closeTwo));
                            tempChange = tempChange * 100;
                            tempChange = Math.round(tempChange);
                            tempChange = tempChange / 100;
                            float tempChangePercent = (tempChange / (Float.parseFloat(closeTwo))) * 100;
                            tempChangePercent = tempChangePercent * 100;
                            tempChangePercent = Math.round(tempChangePercent);
                            tempChangePercent = tempChangePercent / 100;

//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> tempChange -> \n " + tempChange);
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> tempChangePercent -> \n " + tempChangePercent);
                            float temp = (Float.parseFloat(closeOne));
                            temp = temp * 100;
                            temp = Math.round(temp);
                            temp = temp / 100;

                            String lastPrice = String.valueOf(temp);

                            String change = String.valueOf(tempChange);
                            String changePercent = String.valueOf(tempChangePercent);

                            temp = (Float.parseFloat(openOne));
                            temp = temp * 100;
                            temp = Math.round(temp);
                            temp = temp / 100;
                            String open = String.valueOf(temp);

                            temp = (Float.parseFloat(closeOne));
                            temp = temp * 100;
                            temp = Math.round(temp);
                            temp = temp / 100;
                            String close = String.valueOf(temp);

                            String volume = volumeOne;

                            float temp_low = (Float.parseFloat(lowOne));
                            temp_low = temp_low * 100;
                            temp_low = Math.round(temp_low);
                            temp_low = temp_low / 100;

                            float temp_high = (Float.parseFloat(highOne));
                            temp_high = temp_high * 100;
                            temp_high = Math.round(temp_high);
                            temp_high = temp_high / 100;

                            String range = String.valueOf(temp_low) + " - " + String.valueOf(temp_high);

//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> symbol -> \n " + tempStr);
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> lastPrice -> \n " + lastPrice);
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> change -> \n " + "" + change + " (" + changePercent + "%) " );
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> changePercent -> \n " + timeStamp);
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> open -> \n " + open );
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> close -> \n " + close);
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> volume -> \n " + range);
//                            Log.d("AC", "addTextChangedListener -> onTextChanged -> range -> \n " + volume);
                            values = new String[]{tempStr, lastPrice, change + " (" + changePercent + "%) ", timeStamp + " 16:00:00 PST", open, close, range, volume};
                            sharedValues = new String[]{tempStr, lastPrice, change, changePercent};
                            Log.d("STOCKDATA", "CurrentFragment -> getStockData -> values -> \n " + values[0]);
                            CustomStockListAdapter stockListAdapter = new CustomStockListAdapter(getActivity(), KEYS, values, IMAGES);
                            stock_list.setAdapter(stockListAdapter);
                            stock_list.setVisibility(View.VISIBLE);
                            p_bar.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        errorMsg.setVisibility(View.VISIBLE);
                        p_bar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Could not get Data from API, try again later!!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(stringRequest);
    }
}
