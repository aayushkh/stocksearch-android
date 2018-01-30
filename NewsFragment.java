package aayush.khanna.com.stocksearcher_droidapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

public class NewsFragment extends Fragment {
    private static final String TAG = "NEWS";
    private String LOG_TAG = "XML";
    private ArrayList<String> titles_temp = new ArrayList<>();
    private ArrayList<String> links_temp = new ArrayList<>();
    private ArrayList<String> pubDates_temp = new ArrayList<>();
    private ArrayList<String> authors_temp = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>();
    private ArrayList<String> links = new ArrayList<>();
    private ArrayList<String> pubDates = new ArrayList<>();
    private ArrayList<String> authors = new ArrayList<>();
    private ListView news_list;
    private int title_checker = 0;
    private int link_checker = 0;
    private TextView errorNews;
    private ProgressBar p_bar;

    String URL;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news, container, false);
        Bundle extras = getActivity().getIntent().getExtras();
        String stocktickersymbol = extras.getString("SYM_CONSTANT");
        Log.d("CurrentFragment", "CurrentFragment -> stocktickersymbol -> \n " + stocktickersymbol);
        URL = "https://seekingalpha.com/api/sa/combined/" + stocktickersymbol + ".xml";
        titles_temp = new ArrayList<>();
        links_temp = new ArrayList<>();
        pubDates_temp = new ArrayList<>();
        authors_temp = new ArrayList<>();
        titles = new ArrayList<>();
        links = new ArrayList<>();
        pubDates = new ArrayList<>();
        authors = new ArrayList<>();
        news_list = (ListView) view.findViewById(R.id.news_list_view);
        errorNews = (TextView) view.findViewById(R.id.error_news);
        p_bar = (ProgressBar) view.findViewById(R.id.progress_bar_news);
        errorNews.setVisibility(View.GONE);
        p_bar.setVisibility(View.VISIBLE);

        new GetXMLFromServer().execute();

        return view;
    }

    public void ParseXML(String xmlString) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            int eventType = parser.getEventType();
            int count = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {

                    String name = parser.getName();

                    if (name.equals("title")) {
                        if (title_checker == 1) {
                            if (parser.next() == XmlPullParser.TEXT) {
                                String temp = parser.getText();
//                                Log.d(LOG_TAG, "Title:" + temp);
                                titles_temp.add(temp);
                            }
                        } else {
                            title_checker = 1;
                        }
                    } else if (name.equals("link")) {
                        if (link_checker == 1) {
                            if (parser.next() == XmlPullParser.TEXT) {
                                String temp = parser.getText();
//                                Log.d(LOG_TAG, "link:" + temp);
                                links_temp.add(temp);
                            }
                        } else {
                            link_checker = 1;
                        }
                    } else if (name.equals("pubDate")) {

                        if (parser.next() == XmlPullParser.TEXT) {
                            String temp = parser.getText();
                            temp = temp.substring(0, temp.length() - 5);
                            temp = temp + "PST";
//                            Log.d(LOG_TAG, "pubDate:" + temp);
                            pubDates_temp.add(temp);
                        }
                    } else if (name.equals("author_name")) {
                        if (parser.next() == XmlPullParser.TEXT) {
                            String temp = parser.getText();
//                            Log.d(LOG_TAG, "authorname:" + temp);
                            authors_temp.add(temp);
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                }
                eventType = parser.next();
                count++;
            }
            for (int i = 0; i < titles_temp.size(); i++) {
                if ((links_temp.get(i)).contains("article")) {
                    Log.d(LOG_TAG, "title_temp:" + titles_temp.get(i));
                    Log.d(LOG_TAG, "links_temp:" + links_temp.get(i));
                    Log.d(LOG_TAG, "pubdates_temp:" + pubDates_temp.get(i));
                    Log.d(LOG_TAG, "authors_temp:" + authors_temp.get(i));
                    titles.add(titles_temp.get(i));
                    links.add(links_temp.get(i));
                    pubDates.add(pubDates_temp.get(i));
                    authors.add(authors_temp.get(i));
                }
            }
            CustomNewsListAdapter stockListAdapter = new CustomNewsListAdapter(getActivity(), titles, pubDates, authors);
            news_list.setAdapter(stockListAdapter);
            p_bar.setVisibility(View.GONE);
            news_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(links_temp.get(position)));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        errorNews.setVisibility(View.VISIBLE);
                        p_bar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), "Sorry could not get the news article, try again later !", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //RESETTING FLAGS
            title_checker = 0;
            link_checker = 0;
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error in ParseXML()", e);
        }
    }

    private class GetXMLFromServer extends AsyncTask<String, Void, String> {

        HttpHandler nh;

        @Override
        protected String doInBackground(String... strings) {

            String res = "";
            nh = new HttpHandler();
            InputStream is = nh.CallServer(URL);
            if (is != null) {

                res = nh.StreamToString(is);

            } else {
                res = "NotConnected";
            }

            return res;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals("NotConnected")) {

                Toast.makeText(getActivity(), "Connection Error", Toast.LENGTH_SHORT).show();

            } else {
                ParseXML(result);
            }
        }
    }
}



