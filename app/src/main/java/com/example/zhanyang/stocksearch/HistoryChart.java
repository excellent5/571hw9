package com.example.zhanyang.stocksearch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HistoryChart extends Fragment {

    public HistoryChart() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history_chart, container, false);
        Bundle data = getArguments();
        final String symbol = data.getString("symbol");
        final WebView historychart = (WebView) v.findViewById(R.id.historychart);
        WebSettings setting = historychart.getSettings();
        setting.setJavaScriptEnabled(true);
        historychart.loadUrl("file:///android_asset/historychart.html");
        historychart.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                historychart.loadUrl("javascript:init('" + symbol + "')");
            }
        });
        return v;
    }


}
