package com.example.zhanyang.stocksearch;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class News extends Fragment {

    public News() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_news, container, false);
        ListView news = (ListView) v.findViewById(R.id.news);
        Bundle data = getArguments();
        String symbol = data.getString("symbol");
        new NewsTask(news).execute(symbol);
        return v;
    }

    private class NewsTask extends AsyncTask<String, Void, String> {
        ListView lv;

        public NewsTask(ListView lv) {
            this.lv = lv;
        }

        @Override
        protected String doInBackground(String... strings) {
            return MainActivity.getHttpResponse("http://ec2-52-25-115-38.us-west-2.compute.amazonaws.com/571hw8/proxy.php?newssymbol=" + strings[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jobj = new JSONObject(result);
                JSONArray jarr = jobj.getJSONObject("d").getJSONArray("results");
                lv.setAdapter(new NewsListViewAdapter(jarr));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class NewsListViewAdapter extends BaseAdapter {
        JSONArray contents;

        public NewsListViewAdapter(JSONArray contents) {
            this.contents = contents;
        }

        @Override
        public int getCount() {
            return contents.length();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                view = inflater.inflate(R.layout.newsdetails, viewGroup, false);
            }
            try{
                TextView title = (TextView) view.findViewById(R.id.newstitle);
                title.setMovementMethod(LinkMovementMethod.getInstance());
                JSONObject currentdata = contents.getJSONObject(i);
                String titlestring = currentdata.getString("Title");
                String url = currentdata.getString("Url");
                String html = String.format("<a href = \"%s\"><u>%s</u></a>", url, titlestring);
                title.setText(Html.fromHtml(html));
                title.setTextColor(Color.BLACK);
                TextView content = (TextView) view.findViewById(R.id.newscontent);
                content.setText(currentdata.getString("Description"));
                TextView publisher = (TextView) view.findViewById(R.id.publisher);
                publisher.setText("Publisher: "+ currentdata.getString("Source"));
                TextView date = (TextView) view.findViewById(R.id.date);
                String datestring = currentdata.getString("Date");
                SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat out = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss");
                date.setText("Date: " + out.format(in.parse(datestring)));
            }
            catch (JSONException e){
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return view;
        }
    }

}
