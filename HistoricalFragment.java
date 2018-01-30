package aayush.khanna.com.stocksearcher_droidapp;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HistoricalFragment extends Fragment{
    private static final String TAG = "HISTORICAL";
    WebView webView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.historical,container,false);
        Bundle extras = getActivity().getIntent().getExtras();
        final String stocktickersymbol = extras.getString("SYM_CONSTANT");
        webView = (WebView) view.findViewById(R.id.webview_hist);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/Historical.html");
        webView.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                webView.loadUrl("javascript:getData('" + stocktickersymbol + "')");
            }
        });

        return view;
    }
}