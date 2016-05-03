package com.example.zhanyang.stocksearch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.Set;

public class ResultActivity extends AppCompatActivity {

    public Set<String> getFavoriteSymbols() {
        SharedPreferences prefs = getSharedPreferences("favoritelist", MODE_PRIVATE);
        Set<String> symbols = prefs.getStringSet("symbol", null);
        if(symbols == null){
            symbols = new LinkedHashSet<>();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("symbol", symbols);
            editor.apply();
        }
        return symbols;
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
        ViewPager page = (ViewPager) findViewById(R.id.page);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        FragmentManager manager = getSupportFragmentManager();
        String jsonstring = intent.getStringExtra("quotedetail");
        try {
            JSONObject jobj = new JSONObject(jsonstring);
            Bundle bundle = new Bundle();
            final String symbol = jobj.getString("Symbol");
            final String companyname = jobj.getString("Name");
            final Set<String> symbols = getFavoriteSymbols();
            if(symbols.contains(symbol)){
                star.setImageResource(R.drawable.star2);
                star.setTag(R.drawable.star2);
            }
            else{
                star.setImageResource(R.drawable.star);
                star.setTag(R.drawable.star);
            }
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if((Integer) (view.getTag()) == R.drawable.star){
                        ((ImageButton)view).setImageResource(R.drawable.star2);
                        view.setTag(R.drawable.star2);
                        symbols.add(symbol);
                        Toast.makeText(ResultActivity.this, "Bookmarked " + companyname + "!!!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        ((ImageButton)view).setImageResource(R.drawable.star);
                        view.setTag(R.drawable.star);
                        symbols.remove(symbol);
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


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        Bundle data;

        public MyPagerAdapter(FragmentManager fm, Bundle data) {
            super(fm);
            this.data = data;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment selectedfrag = null;
            Log.e("Clicked Position", String.valueOf(position));
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
