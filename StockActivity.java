package aayush.khanna.com.stocksearcher_droidapp;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
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
import java.util.Iterator;

public class StockActivity extends AppCompatActivity {

    private static final String TAG = "STOCKACTIVITY";
    public static String lastPrice, change, changePercent, timeStamp, open, close, volume, range;
    //    public StockDetails entries;
    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        Log.d(TAG, "onCreate: Starting");
//        String stocktickersymbol = getIntent().getStringExtra(MainActivity."SYM_CONSTANT");
        Bundle extras = getIntent().getExtras();
        String stocktickersymbol = null;
        if (extras != null) {
            stocktickersymbol = extras.getString("SYM_CONSTANT");
        }
        setTitle(stocktickersymbol);
        Log.d("StockActivity", "StockActivity -> stocktickersymbol -> \n " + stocktickersymbol);

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        //Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Log.d("StockQuote", "onCreate -> stocktickersymbol: " + stocktickersymbol);


        //LIST-VIEW-ADAPTER FOR STOCK DETAILS TABLE
//        ArrayAdapter<String> stockAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,android.R.id.text1, stockHeaders);
//        ListView lv = findViewById(R.id.stock_list);
//        lv.setAdapter(stockAdapter);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new CurrentFragment(), "CURRENT");
        adapter.addFragment(new HistoricalFragment(), "HISTORICAL");
        adapter.addFragment(new NewsFragment(), "NEWS");
        viewPager.setAdapter(adapter);
    }

}
