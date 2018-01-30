package aayush.khanna.com.stocksearcher_droidapp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;
    public AutoCompleteTextView autoCompleteTextView;
    public List<String> displayAutocomplete;
    public ArrayAdapter<String> autocompleteAdapter;
    String[] KEYS = {"Stock Symbol", "Last Price", "Change", "TimeStamp", "Open", "Close", "Day's Range", " Volume"};
    private ArrayList<String> favListValues;
    private ArrayList<String> symbolArr;
    private ArrayList<String> priceArr;
    private ArrayList<String> changeArr;
    private ArrayList<String> changePercentArr;
    private ListView fav_list;
    private Switch simpleSwitch;
    private ImageView refresh;
    CustomFavListAdapter favListAdapter;
    private ProgressBar barRefresh;
    private int flagOrder = 1;
    private int flagSort = 1;
    Timer timer;
    TimerTask myTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e) {
            Toast.makeText(mActivity, "Could not hide the TitleBar", Toast.LENGTH_SHORT).show();
        }
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate Innitialised");

        fav_list = (ListView) findViewById(R.id.fav_list_view);
        barRefresh = (ProgressBar) findViewById(R.id.progress_bar_refresh);
        barRefresh.setVisibility(View.INVISIBLE);
        refresh = (ImageView) findViewById(R.id.popup_sync);
        simpleSwitch = (Switch) findViewById(R.id.auto_refresh);
        mContext = getApplicationContext();
        mActivity = MainActivity.this;
        symbolArr = new ArrayList<>();
        priceArr = new ArrayList<>();
        changeArr = new ArrayList<>();
        changePercentArr = new ArrayList<>();

        //AUTOCOMPLETE-TEXT-VIEW
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String inputStr = (String) adapterView.getItemAtPosition(position);
//                inputStr = inputStr.split("\n")[0];
//                Log.d("AC", "onItemClick -> inputStr -> \n " + inputStr);
                autoCompleteTextView.setText(inputStr);
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String tempStr = charSequence.toString();
                tempStr = tempStr.trim();
                if (!tempStr.isEmpty()) {
//                    Log.d("AC", "addTextChangedListener -> onTextChanged -> tempStr -> \n " + tempStr);
                    RequestQueue requestQueue = Volley.newRequestQueue(mContext);
                    String urlAC = "https://bootstrap-stocksearch.appspot.com/autocomplete?stocktickersymbol=" + tempStr;
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, urlAC,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
//                                    Log.d("AC", "addTextChangedListener -> onTextChanged -> response -> \n " + response);
                                    JSONArray jsonArr = null;
                                    try {
                                        jsonArr = new JSONArray(response);
//                                        Log.d("AC", "addTextChangedListener -> onTextChanged -> jsonArr -> \n " + jsonArr);

                                        displayAutocomplete = new ArrayList<String>();
                                        for (int i = 0; i < jsonArr.length(); i++) {
                                            JSONObject jsonObj = null;
                                            String symbol = "";
                                            String name = "";
                                            String exchng = "";
                                            try {
                                                jsonObj = jsonArr.getJSONObject(i);
                                                symbol = jsonObj.getString("Symbol");
                                                name = jsonObj.getString("Name");
                                                exchng = jsonObj.getString("Exchange");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            String display = symbol + " - " + name + " (" + exchng + ")";
                                            displayAutocomplete.add(display);
                                        }
                                        Log.d("AC", "addTextChangedListener -> onTextChanged -> displayAutocomplete -> \n " + displayAutocomplete);
                                        autocompleteAdapter = new ArrayAdapter<String>(mContext, R.layout.list_autocomplete, displayAutocomplete);
                                        autoCompleteTextView.setAdapter(autocompleteAdapter);
                                        autoCompleteTextView.setThreshold(0);
                                        autocompleteAdapter.notifyDataSetChanged();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(mContext, "Error with AutoComplete Volley!", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    requestQueue.add(stringRequest);
                } else {
                    Toast.makeText(mContext, "Please Enter a Stock Ticker Symbol", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //REFRESH BUTTON FUNCTIONALITY
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //START PROGRESSBAR

                barRefresh = (ProgressBar) findViewById(R.id.progress_bar_refresh);
                barRefresh.setVisibility(View.VISIBLE);

                symbolArr = new ArrayList<>();
                priceArr = new ArrayList<>();
                changeArr = new ArrayList<>();
                changePercentArr = new ArrayList<>();

                ArrayList<String> symbols = new ArrayList<String>();
                SharedPreferences sharedPrefTwo = getSharedPreferences("favourites", Context.MODE_PRIVATE);
                Map<String, ?> keys = sharedPrefTwo.getAll();

                // GET THE SYMBOLS
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    String[] tempVar = entry.getValue().toString().split(" ");
                    symbols.add(tempVar[0]);
                }


                //CLEAR THE SHARED PREFERENCES

//                sharedPrefTwo.edit().clear().commit();

                for (int i = 0; i < symbols.size(); i++) {

                    final String tempStr = symbols.get(i);
                    Log.d("REFRESH", ": " + tempStr);

                    RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                    String urlPrice = "https://bootstrap-stocksearch.appspot.com/compact?stocktickersymbol=" + tempStr;
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, urlPrice,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
//                        Log.d("REFRESH", "response -> \n " + response);
                                    try {
                                        JSONObject jsonObj = new JSONObject(response);
//                            Log.d("REFRESH", "addTextChangedListener -> onTextChanged -> jsonObj -> \n " + jsonObj);
                                        String timeStamp = jsonObj.getJSONObject("Meta Data").getString("3. Last Refreshed");
                                        JSONObject timeSeries = jsonObj.getJSONObject("Time Series (Daily)");
//                            Log.d("REFRESH", "addTextChangedListener -> onTextChanged -> timeSeries -> \n " + timeSeries);
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
                                        symbolArr.add(tempStr);
                                        priceArr.add(lastPrice);
                                        changeArr.add(change);
                                        changePercentArr.add(changePercent);

                                        favListAdapter = new CustomFavListAdapter(MainActivity.this, symbolArr, priceArr, changeArr, changePercentArr);
                                        fav_list.setAdapter(favListAdapter);
                                        favListAdapter.notifyDataSetChanged();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(MainActivity.this, "Could not get Data from API, try again later!!", Toast.LENGTH_SHORT).show();
                                }
                            });
                    requestQueue.add(stringRequest);

                }

                barRefresh.setVisibility(View.INVISIBLE);
            }
        });

        //SPINNER-SORTBY
        final Spinner sortSpinner = findViewById(R.id.spinner_sort);
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(this, R.array.SortBy, android.R.layout.simple_spinner_item);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        //SPINNER-ORDERBY
        final Spinner orderSpinner = findViewById(R.id.spinner_order);
        ArrayAdapter<CharSequence> orderAdapter = ArrayAdapter.createFromResource(this, R.array.OrderBy, android.R.layout.simple_spinner_item);
        orderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(orderAdapter);

        //SPINNER FUNCTIONS
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                if (flagSort != 1) {

                    String textOrder = orderSpinner.getSelectedItem().toString();
                    String textSort = sortSpinner.getSelectedItem().toString();
                    SharedPreferences sharedPrefTwo = getSharedPreferences("favourites", Context.MODE_PRIVATE);
                    Map<String, ?> keys = sharedPrefTwo.getAll();

                    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
                    ArrayList<String> tempList;

                    for (int i = 0; i < symbolArr.size(); i++) {
                        tempList = new ArrayList<String>();
                        for (int j = 0; j < 4; j++) {
                            if (j == 0) {
                                tempList.add(symbolArr.get(i));
                            } else if (j == 1) {
                                tempList.add(priceArr.get(i));
                            } else if (j == 2) {
                                tempList.add(changeArr.get(i));
                            } else if (j == 3) {
                                tempList.add(changePercentArr.get(i));
                            }
                        }
                        list.add(tempList);
                    }

                    ArrayList<String> tempSym = new ArrayList<>();
                    ArrayList<String> tempPrice = new ArrayList<>();
                    ArrayList<String> tempChange = new ArrayList<>();
                    ArrayList<String> tempChangePercent = new ArrayList<>();

                    if (textOrder.matches("Ascending") && textSort.matches("Name")) {

                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return a.get(0).compareTo(b.get(0));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Ascending") && textSort.matches("Price")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(a.get(1)), Float.parseFloat(b.get(1)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Ascending") && textSort.matches("Change")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(a.get(2)), Float.parseFloat(b.get(2)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Ascending") && textSort.matches("Change Percent")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(a.get(3)), Float.parseFloat(b.get(3)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Descending") && textSort.matches("Name")) {

                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return b.get(0).compareTo(a.get(0));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Descending") && textSort.matches("Price")) {

                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(b.get(1)), Float.parseFloat(a.get(1)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Descending") && textSort.matches("Change")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(b.get(2)), Float.parseFloat(a.get(2)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);


                    } else if (textOrder.matches("Descending") && textSort.matches("Change Percent")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(b.get(3)), Float.parseFloat(a.get(3)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    }

                    favListAdapter = new CustomFavListAdapter(MainActivity.this, symbolArr, priceArr, changeArr, changePercentArr);
                    fav_list.setAdapter(favListAdapter);

                } else {
                    flagSort = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                if (flagOrder != 1) {

                    String textOrder = orderSpinner.getSelectedItem().toString();
                    String textSort = sortSpinner.getSelectedItem().toString();
                    SharedPreferences sharedPrefTwo = getSharedPreferences("favourites", Context.MODE_PRIVATE);
                    Map<String, ?> keys = sharedPrefTwo.getAll();

                    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
                    ArrayList<String> tempList;

                    for (int i = 0; i < symbolArr.size(); i++) {
                        tempList = new ArrayList<String>();
                        for (int j = 0; j < 4; j++) {
                            if (j == 0) {
                                tempList.add(symbolArr.get(i));
                            } else if (j == 1) {
                                tempList.add(priceArr.get(i));
                            } else if (j == 2) {
                                tempList.add(changeArr.get(i));
                            } else if (j == 3) {
                                tempList.add(changePercentArr.get(i));
                            }
                        }
                        list.add(tempList);
                    }

                    ArrayList<String> tempSym = new ArrayList<>();
                    ArrayList<String> tempPrice = new ArrayList<>();
                    ArrayList<String> tempChange = new ArrayList<>();
                    ArrayList<String> tempChangePercent = new ArrayList<>();

                    if (textOrder.matches("Ascending") && textSort.matches("Name")) {

                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return a.get(0).compareTo(b.get(0));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Ascending") && textSort.matches("Price")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(a.get(1)), Float.parseFloat(b.get(1)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Ascending") && textSort.matches("Change")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(a.get(2)), Float.parseFloat(b.get(2)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Ascending") && textSort.matches("Change Percent")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(a.get(3)), Float.parseFloat(b.get(3)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Descending") && textSort.matches("Name")) {

                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return b.get(0).compareTo(a.get(0));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Descending") && textSort.matches("Price")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(b.get(1)), Float.parseFloat(a.get(1)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Descending") && textSort.matches("Change")) {

                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(b.get(2)), Float.parseFloat(a.get(2)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    } else if (textOrder.matches("Descending") && textSort.matches("Change Percent")) {


                        Collections.sort(list, new Comparator<ArrayList<String>>() {
                            @Override
                            public int compare(ArrayList<String> a, ArrayList<String> b) {
                                return Float.compare(Float.parseFloat(b.get(3)), Float.parseFloat(a.get(3)));
                            }
                        });

                        ArrayList<String> myArray = new ArrayList<String>();

                        for (ArrayList<String> innerList : list) {
                            for (String tempStr : innerList) {
                                myArray.add(tempStr);
                            }
                        }

                        for (int i = 0; i < myArray.size(); i++) {
                            if (i % 4 == 0) {
                                tempSym.add(myArray.get(i));
                            } else if (i % 4 == 1) {
                                tempPrice.add(myArray.get(i));
                            } else if (i % 4 == 2) {
                                tempChange.add(myArray.get(i));
                            } else if (i % 4 == 3) {
                                tempChangePercent.add(myArray.get(i));
                            }
                        }


                        symbolArr.removeAll(symbolArr);
                        priceArr.removeAll(priceArr);
                        changeArr.removeAll(changeArr);
                        changePercentArr.removeAll(changePercentArr);

                        symbolArr = new ArrayList<String>(tempSym);
                        priceArr = new ArrayList<String>(tempPrice);
                        changeArr = new ArrayList<String>(tempChange);
                        changePercentArr = new ArrayList<String>(tempChangePercent);

                    }

                    favListAdapter = new CustomFavListAdapter(MainActivity.this, symbolArr, priceArr, changeArr, changePercentArr);
                    fav_list.setAdapter(favListAdapter);

                } else {
                    flagOrder = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        //FAV ONCLICK GET STOCK
        fav_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String stocktickersymbol = symbolArr.get(position);

                Intent intent = new Intent(mActivity, StockActivity.class);
                intent.putExtra("SYM_CONSTANT", stocktickersymbol);
                startActivity(intent);
            }
        });


        //FAV DELETE IMPLEMENTATION

        fav_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                Toast.makeText(mActivity, "Long Clicked " + symbolArr.get(position), Toast.LENGTH_SHORT).show();
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        String tempStr = (String) item.getTitle().toString();
                        if (tempStr.contains("No")) {
                            Toast.makeText(MainActivity.this, "Not Deleting ", Toast.LENGTH_SHORT).show();
                        } else if (tempStr.contains("Yes")) {
                            Toast.makeText(MainActivity.this, "Deleted ", Toast.LENGTH_SHORT).show();
                            String symbol = symbolArr.get(position);
                            SharedPreferences sharedPrefTwo = getSharedPreferences("favourites", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPrefTwo.edit();
                            editor.remove(symbol);
                            editor.apply();
                            refresh();
                        }
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });

        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Boolean state = simpleSwitch.isChecked();
                if (state) {
                    Toast.makeText(MainActivity.this, "SWITCH - ON", Toast.LENGTH_SHORT).show();
                    timer = new Timer();
                    myTask = new TimerTask() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    };

                    timer.schedule(myTask, 1000, 10000);
                } else {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                }
            }
        });

    }

    private void refresh() {

        symbolArr = new ArrayList<>();
        priceArr = new ArrayList<>();
        changeArr = new ArrayList<>();
        changePercentArr = new ArrayList<>();

        ArrayList<String> symbols = new ArrayList<String>();
        SharedPreferences sharedPrefTwo = getSharedPreferences("favourites", Context.MODE_PRIVATE);
        Map<String, ?> keys = sharedPrefTwo.getAll();

        // GET THE SYMBOLS
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String[] tempVar = entry.getValue().toString().split(" ");
            symbols.add(tempVar[0]);
        }

        Log.d("REFRESH", "symbols size -> " + symbols.size());

        if (symbols.size() == 0) {
        fav_list.setVisibility(View.INVISIBLE);
        }
        //CLEAR THE SHARED PREFERENCES

//                sharedPrefTwo.edit().clear().commit();
        else {
            for (int i = 0; i < symbols.size(); i++) {

                final String tempStr = symbols.get(i);
                Log.d("REFRESH", ": " + tempStr);

                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                String urlPrice = "https://bootstrap-stocksearch.appspot.com/compact?stocktickersymbol=" + tempStr;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, urlPrice,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
//                        Log.d("REFRESH", "response -> \n " + response);
                                try {
                                    JSONObject jsonObj = new JSONObject(response);
//                            Log.d("REFRESH", "addTextChangedListener -> onTextChanged -> jsonObj -> \n " + jsonObj);
                                    String timeStamp = jsonObj.getJSONObject("Meta Data").getString("3. Last Refreshed");
                                    JSONObject timeSeries = jsonObj.getJSONObject("Time Series (Daily)");
//                            Log.d("REFRESH", "addTextChangedListener -> onTextChanged -> timeSeries -> \n " + timeSeries);
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
                                    symbolArr.add(tempStr);
                                    priceArr.add(lastPrice);
                                    changeArr.add(change);
                                    changePercentArr.add(changePercent);

                                    favListAdapter = new CustomFavListAdapter(MainActivity.this, symbolArr, priceArr, changeArr, changePercentArr);
                                    fav_list.setAdapter(favListAdapter);
                                    favListAdapter.notifyDataSetChanged();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Could not get some Data from API, try again later!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                requestQueue.add(stringRequest);

            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        addFavourites();
    }

    private void addFavourites() {

        symbolArr = new ArrayList<>();
        priceArr = new ArrayList<>();
        changeArr = new ArrayList<>();
        changePercentArr = new ArrayList<>();

        ArrayList<String> symbols = new ArrayList<String>();
        SharedPreferences sharedPrefTwo = getSharedPreferences("favourites", Context.MODE_PRIVATE);
        Map<String, ?> keys = sharedPrefTwo.getAll();

        // GET THE SYMBOLS
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String[] tempVar = entry.getValue().toString().split(" ");
            symbols.add(tempVar[0]);
        }

        //CLEAR THE SHARED PREFERENCES

//                sharedPrefTwo.edit().clear().commit();
        for (int i = 0; i < symbols.size(); i++) {

            final String tempStr = symbols.get(i);
            Log.d("REFRESH", ": " + tempStr);

            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            String urlPrice = "https://bootstrap-stocksearch.appspot.com/compact?stocktickersymbol=" + tempStr;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlPrice,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
//                        Log.d("REFRESH", "response -> \n " + response);
                            try {
                                JSONObject jsonObj = new JSONObject(response);
//                            Log.d("REFRESH", "addTextChangedListener -> onTextChanged -> jsonObj -> \n " + jsonObj);
                                String timeStamp = jsonObj.getJSONObject("Meta Data").getString("3. Last Refreshed");
                                JSONObject timeSeries = jsonObj.getJSONObject("Time Series (Daily)");
//                            Log.d("REFRESH", "addTextChangedListener -> onTextChanged -> timeSeries -> \n " + timeSeries);
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
                                symbolArr.add(tempStr);
                                priceArr.add(lastPrice);
                                changeArr.add(change);
                                changePercentArr.add(changePercent);

//
//                                String tempPref = symbolArr.get(i) + " " + symbolArr.get(i) + " " + symbolArr.get(i) + " " + symbolArr.get(i);
//                                SharedPreferences.Editor editor = sharedPrefTwo.edit();
//                                editor.putString(symbolArr.get(i), tempPref);
//                                Log.d("SHARED", "SHAREDPREFERENCES !!: " + symbolArr.get(i) + " : " + tempPref);
//                                editor.apply();

                                favListAdapter = new CustomFavListAdapter(MainActivity.this, symbolArr, priceArr, changeArr, changePercentArr);
                                fav_list.setAdapter(favListAdapter);
                                favListAdapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, "Could not get all Data from API, please refresh!!", Toast.LENGTH_SHORT).show();
                        }
                    });
            requestQueue.add(stringRequest);
        }

        Log.d("FAV ON RESUME", "symbolArr -> " + symbolArr);


    }


    public void clearQuote(View view) {
//        Log.d("MainActivity", "Clear Function invoked");
        Toast.makeText(getApplicationContext(), "Cleared !", Toast.LENGTH_SHORT).show();
        autoCompleteTextView.setText("");
    }

    public void getQuote(View view) {
//        Log.d("MainActivity", "Get Function invoked");
        boolean check = true;
        String stocktickersymbol = autoCompleteTextView.getText().toString();
        if (stocktickersymbol.trim().matches("")) {
            Toast.makeText(mContext, "Please enter a stock name or symbol", Toast.LENGTH_SHORT).show();
            check = false;
        } else {
            stocktickersymbol = stocktickersymbol.split(" ")[0];
        }
//        Log.d("MainActivity", "getQuote -> \n" + stocktickersymbol);
        if (check) {
            Toast.makeText(mContext, "Getting " + stocktickersymbol + " Quote", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(mActivity, StockActivity.class);
            intent.putExtra("SYM_CONSTANT", stocktickersymbol);
            startActivity(intent);
        }
    }

}
