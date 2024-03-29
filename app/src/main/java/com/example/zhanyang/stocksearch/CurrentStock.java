package com.example.zhanyang.stocksearch;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;


public class CurrentStock extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_current_stock, container, false);
        ListView stockdetail = (ListView) v.findViewById(R.id.stockdetail);
        View footer = View.inflate(getContext(), R.layout.listviewfooter, null);
        stockdetail.addFooterView(footer);
        Bundle data = getArguments();
        String symbol = data.getString("symbol");
        new QuotesTask(stockdetail).execute(symbol);
        new PictureTask(footer, stockdetail).execute(symbol);
        return v;
    }

    private class QuotesTask extends AsyncTask<String, Void, String> {
        ListView lv;

        public QuotesTask(ListView lv) {
            this.lv = lv;
        }

        @Override
        protected String doInBackground(String... strings) {
            return MainActivity.getHttpResponse("http://ec2-52-25-115-38.us-west-2.compute.amazonaws.com/571hw8/proxy.php?stocksymbol=" + strings[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jobj = new JSONObject(result);
                List<String> contents = new ArrayList<>();
                String stockname = jobj.getString("Name");
                String stocksymbol = jobj.getString("Symbol");
                String lastprice = jobj.getString("LastPrice");
                Double d1 = Double.valueOf(jobj.getString("Change"));
                Double d2 = Double.valueOf(jobj.getString("ChangePercent"));
                String change = String.format("%.2f", d1) + "(" + ((d2 > 0) ? "+" : "") + String.format("%.2f", d2) +"%)";
                String timestamp = jobj.getString("Timestamp");
                SimpleDateFormat in = new SimpleDateFormat("EEE MMMM d HH:mm:ss 'UTC-04:00' yyyy");
                SimpleDateFormat out = new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss");
                String date = out.format(in.parse(timestamp));
                Log.e("Transformed date", date);
                String marketCap = jobj.getString("MarketCap");
                Double mc = Double.valueOf(marketCap);
                if (mc >= 1000000000) {
                    marketCap = String.format("%.2f", mc / 1000000000) + "Billion";
                } else if (mc >= 1000000) {
                    marketCap = String.format("%.2f", mc / 1000000) + "Million";
                }
                String volume = jobj.getString("Volume");
                Double d3 = Double.valueOf(jobj.getString("ChangeYTD"));
                Double d4 = Double.valueOf(jobj.getString("ChangePercentYTD"));
                String changeYTD = String.format("%.2f", d3) + "(" + ((d4 > 0) ? "+" : "") + String.format("%.2f", d4) + "%)";
                String high = jobj.getString("High");
                String low = jobj.getString("Low");
                String open = jobj.getString("Open");
                contents.add(stockname);
                contents.add(stocksymbol);
                contents.add(lastprice);
                contents.add(change);
                contents.add(date);
                contents.add(marketCap);
                contents.add(volume);
                contents.add(changeYTD);
                contents.add(high);
                contents.add(low);
                contents.add(open);
                if (d1 > 0) {
                    contents.add("up");
                } else if (d1 < 0) {
                    contents.add("down");
                } else {
                    contents.add("");
                }

                if (d4 > 0) {
                    contents.add("up");
                } else if (d4 < 0) {
                    contents.add("down");
                } else {
                    contents.add("");
                }
                lv.setAdapter(new QuoteListViewAdapter(contents));
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private class QuoteListViewAdapter extends BaseAdapter {
        String[] theads = new String[]{"NAME", "SYMBOL", "LASTPRICE", "CHANGE", "TIMESTAMP",
                "MARKETCAP", "VOLUME", "CHANGE YTD", "HIGH", "LOW", "OPEN"};
        List<String> contents;
        final int WITH_ARROW = 1;
        final int WITHOUT_ARROW = 0;

        public QuoteListViewAdapter(List<String> contents) {
            this.contents = contents;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 3 || position == 7){
                return WITH_ARROW;
            }
            return WITHOUT_ARROW;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getCount() {
            return 11;
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
                int type = getItemViewType(i);
                if(type == WITH_ARROW){
                    view = inflater.inflate(R.layout.quotedetails, viewGroup, false);
                }
                else{
                    view = inflater.inflate(R.layout.quotedetails_woarrow, viewGroup, false);
                }
            }
            TextView thead = (TextView) view.findViewById(R.id.thead);
            thead.setText(theads[i]);
            TextView content = (TextView) view.findViewById(R.id.content);
            content.setText(contents.get(i));
            if (i == 3 || i == 7) {
                ImageView arrow = (ImageView) view.findViewById(R.id.arrow);
                String arrowname = contents.get(11 + i / 4);
                if (arrowname.equals("up")) {
                    arrow.setImageResource(R.drawable.up);
                } else if (arrowname.equals("down")) {
                    arrow.setImageResource(R.drawable.down);
                }
            }
            return view;
        }
    }

    private class PictureTask extends AsyncTask<String, Void, Bitmap> {
        View footer;
        ListView lv;

        public PictureTask(View footer, ListView lv) {
            this.footer = footer;
            this.lv = lv;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            Bitmap pic = null;
            try {
                URL url = new URL("http://chart.finance.yahoo.com/t?s=" + strings[0] + "&lang=en-US&height=500&width=900");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = urlConnection.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                pic = BitmapFactory.decodeStream(is, null, options);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return pic;
        }

        @Override
        protected void onPostExecute(final Bitmap pic) {
            final ImageView image = (ImageView) footer.findViewById(R.id.yahoochart);;
            image.setImageBitmap(pic);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog d = new Dialog(getActivity());
                    d.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    ImageView popupimage = new ImageView(getActivity());
                    popupimage.setMinimumWidth(900);
                    popupimage.setMinimumHeight(500);
                    popupimage.setImageBitmap(pic);
                    PhotoViewAttacher mAttacher = new PhotoViewAttacher(popupimage);
                    d.setContentView(popupimage);
                    d.show();
                }
            });

        }
    }
}
