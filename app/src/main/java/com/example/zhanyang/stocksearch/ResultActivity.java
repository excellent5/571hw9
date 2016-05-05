package com.example.zhanyang.stocksearch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    CallbackManager callbackManager;

    public List<String> getFavoriteSymbols() {
        SharedPreferences prefs = getSharedPreferences("favoritelist", MODE_PRIVATE);
        String symbols = prefs.getString("symbol", null);
        if(symbols == null){
            return new LinkedList<>();
        }
        return new LinkedList<>(Arrays.asList(symbols.split(",")));
    }

    public void store2DB(List<String> symbols){
        SharedPreferences prefs = getSharedPreferences("favoritelist", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("symbol", TextUtils.join(",", symbols));
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar tb = (Toolbar) findViewById(R.id.mytoolbar);
        setSupportActionBar(tb);

        ImageButton back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton star = (ImageButton) findViewById(R.id.star);

        Intent intent = getIntent();
        MyViewPager page = (MyViewPager)findViewById(R.id.page);
        page.setPagingEnabled(false);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        FragmentManager manager = getSupportFragmentManager();
        String jsonstring = intent.getStringExtra("quotedetail");
        try {
            JSONObject jobj = new JSONObject(jsonstring);
            Bundle bundle = new Bundle();
            final String symbol = jobj.getString("Symbol");
            final String companyname = jobj.getString("Name");
            final Double price = jobj.getDouble("LastPrice");
            final List<String> symbols = getFavoriteSymbols();
            if(symbols.contains(symbol)){
                star.setImageResource(R.drawable.star2);
                star.setTag(R.drawable.star2);
            }
            else{
                star.setImageResource(R.drawable.star);
                star.setTag(R.drawable.star);
            }
            TextView company = (TextView) findViewById(R.id.companyname);
            company.setText(companyname);

            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if((Integer) (view.getTag()) == R.drawable.star){
                        ((ImageButton)view).setImageResource(R.drawable.star2);
                        view.setTag(R.drawable.star2);
                        symbols.add(symbol);
                        store2DB(symbols);
                        Toast.makeText(ResultActivity.this, "Bookmarked " + companyname + "!!!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        ((ImageButton)view).setImageResource(R.drawable.star);
                        view.setTag(R.drawable.star);
                        symbols.remove(symbol);
                        store2DB(symbols);
                    }
                }
            });

            FacebookSdk.sdkInitialize(getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            final ShareDialog shareDialog = new ShareDialog(this);
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

                @Override
                public void onSuccess(Sharer.Result result) {
                    Toast.makeText(ResultActivity.this, "You shared this post.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(ResultActivity.this, "Whoops! Error happens!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(ResultActivity.this, "You canceled sharing.", Toast.LENGTH_LONG).show();
                }
            });

            ImageButton fb = (ImageButton) findViewById(R.id.facebook);
            fb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(shareDialog.canShow(ShareLinkContent.class)){
                        ShareLinkContent content = new ShareLinkContent.Builder()
                                .setContentUrl(Uri.parse("http://finance.yahoo.com/q?s=" + symbol))
                                .setImageUrl(Uri.parse("http://chart.finance.yahoo.com/t?s=" + symbol + "&lang=en-US&width=400&height=400"))
                                .setContentDescription("Stock Information of " + companyname + " (" + symbol + ")")
                                .setContentTitle("Current Stock Price of " + companyname + " is $" + String.format("%.2f", price))
                                .build();
                        shareDialog.show(content);
                    }
                }
            });

            bundle.putString("symbol", symbol);
            MyPagerAdapter pageadapter = new MyPagerAdapter(manager, bundle);
            page.setAdapter(pageadapter);
            tabs.setupWithViewPager(page);
            page.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        Bundle data;

        public MyPagerAdapter(FragmentManager fm, Bundle data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment selectedfrag = null;
            if (position == 0) {
                selectedfrag = new CurrentStock();
            } else if(position == 1){
                selectedfrag = new HistoryChart();
            } else if (position == 2) {
                selectedfrag = new News();
            }
            selectedfrag.setArguments(data);
            return selectedfrag;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = "CURRENT";
            if (position == 1) {
                title = "HISTORICAL";
            } else if (position == 2) {
                title = "NEWS";
            }
            return title;
        }

    }
}
