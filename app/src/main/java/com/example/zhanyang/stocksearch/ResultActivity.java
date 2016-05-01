package com.example.zhanyang.stocksearch;

import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Toolbar tb = (Toolbar) findViewById(R.id.mytoolbar);
        setSupportActionBar(tb);

        Intent intent = getIntent();
        MyViewPager page = (MyViewPager) findViewById(R.id.page);
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        FragmentManager manager = getSupportFragmentManager();
        String jsonstring = intent.getStringExtra("quotedetail");
        try {
            JSONObject jobj = new JSONObject(jsonstring);
            Bundle bundle = new Bundle();
            bundle.putString("symbol", jobj.getString("Symbol"));
            MyPagerAdapter pageadapter = new MyPagerAdapter(manager, bundle);
            page.setAdapter(pageadapter);
            tabs.setupWithViewPager(page);
            page.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back:
                //go back
                return true;

            case R.id.star:
                return true;

            case R.id.facebook:
                return true;

            default:
                return super.onOptionsItemSelected(item);
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
