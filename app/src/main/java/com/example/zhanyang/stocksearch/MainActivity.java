package com.example.zhanyang.stocksearch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Handler myhandler = new Handler();
    private Runnable mytask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.completelist);
        textView.setAdapter(new MyAutoCompleteAdapter());

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView selectedsymbol = (TextView) view.findViewById(R.id.symbol);
                textView.setText(selectedsymbol.getText());
            }
        });

        final DynamicListView favoritelist = (DynamicListView) findViewById(R.id.favoritelist);
        favoritelist.enableSwipeToDismiss(new OnDismissCallback() {
            @Override
            public void onDismiss(@NonNull ViewGroup listView, @NonNull int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    Log.e("Removed position: ", String.valueOf(position));
                }
            }
        });

        final FavoritelistTask loadfavoritetask = new FavoritelistTask(favoritelist);
        loadfavoritetask.execute(getFavoriteSymbols());


        Switch autorefresh = (Switch) findViewById(R.id.autofresh);
        autorefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mytask = new Runnable() {
                        @Override
                        public void run() {
                            loadfavoritetask.execute(getFavoriteSymbols());
                        }
                    };
                    myhandler.postDelayed(mytask, 10000);
                } else {
                    myhandler.removeCallbacks(mytask);
                }
            }
        });

        ImageButton refresh = (ImageButton) findViewById(R.id.fresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FavoriteListAdapter myadapter = (FavoriteListAdapter) favoritelist.getAdapter();
//                List<String> adapterdata = myadapter.getAdapterData();
//                adapterdata.set(0, "{\"Status\":\"SUCCESS\",\"Name\":\"Apple Inc\",\"Symbol\":\"AAPL\",\"LastPrice\":120.00,\"ChangePercent\":0.0921319329279476,\"MarketCap\":602363497120}");
//                myadapter.notifyDataSetChanged();
                loadfavoritetask.execute(getFavoriteSymbols());
            }
        });

        Button getquotesbtn = (Button) findViewById(R.id.getquote);
        getquotesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textView.getText().toString().isEmpty()) {
                    showDialog("Please enter a Stock Name/Symbol");
                } else {
                    new QuotesTask().execute(textView.getText().toString());
                }
            }
        });
    }

    public void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public Set<String> getFavoriteSymbols() {
//        SharedPreferences prefs = getSharedPreferences("favoritelist", MODE_PRIVATE);
//        Set<String> symbols = prefs.getStringSet("symbol", null);
        Set<String> symbols = new LinkedHashSet<>();
//        symbols.add("AAPL");
//        symbols.add("FB");
        return symbols;
    }

    public static String getHttpResponse(String urlstring) {
        HttpURLConnection urlConnection = null;
        String result = null;
        try {
            URL url = new URL(urlstring);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            Reader reader = new InputStreamReader(is, "UTF-8");
            StringBuilder sb = new StringBuilder();
            int read = 0;
            int len = 512;
            char[] buffer = new char[len];
            while ((read = reader.read(buffer, 0, len)) != -1) {
                sb.append(buffer, 0, read);
            }
            result = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return result;
    }

    private class MyAutoCompleteAdapter extends BaseAdapter implements Filterable {
        List<StockDetail> stocklist = new ArrayList<>();

        @Override
        public int getCount() {
            return stocklist.size();
        }

        @Override
        public StockDetail getItem(int i) {
            return stocklist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getBaseContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.dropdown_item, viewGroup, false);
            }
            TextView symboltv = (TextView) view.findViewById(R.id.symbol);
            symboltv.setText(getItem(i).symbol);
            TextView desctv = (TextView) view.findViewById(R.id.desc);
            desctv.setText(getItem(i).description);
            return view;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults fr = new FilterResults();
                    if (charSequence != null && charSequence.length() >= 3) {
                        String response = getHttpResponse("http://ec2-52-25-115-38.us-west-2.compute.amazonaws.com/571hw8/proxy.php?lookupsymbol="
                                + charSequence);
                        try {
                            JSONArray jarr = new JSONArray(response);
                            for (int i = 0; i < jarr.length(); i++) {
                                JSONObject jobj = jarr.getJSONObject(i);
                                stocklist.add(new StockDetail(jobj.getString("Symbol"), jobj.getString("Name"), jobj.getString("Exchange")));
                            }
                            fr.count = stocklist.size();
                            fr.values = stocklist;
                            return fr;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    return null;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    if (filterResults != null) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        private class StockDetail {
            public String symbol;
            public String description;

            public StockDetail(String symbol, String name, String exchange) {
                this.symbol = symbol;
                description = name + "(" + exchange + ")";
            }
        }
    }

    private class QuotesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return getHttpResponse("http://ec2-52-25-115-38.us-west-2.compute.amazonaws.com/571hw8/proxy.php?stocksymbol=" + strings[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jobj = new JSONObject(result);
                if (jobj.getString("Status").equals("SUCCESS")) {
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    intent.putExtra("quotedetail", result);
                    startActivity(intent);
                } else {
                    showDialog("Invalid Symbol");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class FavoritelistTask extends AsyncTask<Set<String>, Void, List<String>> {

        String urlstring = "http://ec2-52-25-115-38.us-west-2.compute.amazonaws.com/571hw8/proxy.php?stocksymbol=";
        DynamicListView lv;

        public FavoritelistTask(DynamicListView lv) {
            this.lv = lv;
        }

        @Override
        protected List<String> doInBackground(Set<String>... params) {
            ArrayList<String> jsonresults = new ArrayList<>();
            for (String symbol : params[0]) {
                jsonresults.add(getHttpResponse(urlstring + symbol));
            }
            return jsonresults;
        }

        @Override
        protected void onPostExecute(final List<String> result) {
            FavoriteListAdapter myadapter = new FavoriteListAdapter(result);
            lv.setAdapter(myadapter);
        }
    }

    public class FavoriteListAdapter extends BaseAdapter {
        List<String> result;

        public FavoriteListAdapter(List<String> result) {
            this.result = result;
        }

        public List<String> getAdapterData() {
            return result;
        }

        @Override
        public int getCount() {
            return result.size();
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
                LayoutInflater inflater = (LayoutInflater) getBaseContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.favoritelist, viewGroup, false);
            }
            try {
                JSONObject jsonobj = new JSONObject(result.get(i));
                if (jsonobj.getString("Status").equals("SUCCESS")) {
                    TextView symboltv = (TextView) view.findViewById(R.id.symbol);
                    symboltv.setText(jsonobj.getString("Symbol"));
                    TextView nametv = (TextView) view.findViewById(R.id.name);
                    nametv.setText(jsonobj.getString("Name"));
                    TextView pricetv = (TextView) view.findViewById(R.id.price);
                    pricetv.setText("$" + jsonobj.getString("LastPrice"));
                    TextView percenttv = (TextView) view.findViewById(R.id.percent);
                    Double d = Double.valueOf(jsonobj.getString("ChangePercent"));
                    if (d > 0) {
                        percenttv.setBackgroundColor(Color.GREEN);
                        percenttv.setText("+" + String.format("%.2f", d) + "%");
                    } else if (d < 0) {
                        percenttv.setBackgroundColor(Color.RED);
                        percenttv.setText(String.format("%.2f", d) + "%");
                    }
                    TextView markettv = (TextView) view.findViewById(R.id.marketcap);
                    Double d2 = Double.valueOf(jsonobj.getString("MarketCap"));
                    if (d2 >= 1000000000) {
                        markettv.setText("Market Cap: " + String.format("%.2f", d2 / 1000000000) + " Billion");
                    } else if (d2 >= 1000000) {
                        markettv.setText("Market Cap: " + String.format("%.2f", d2 / 1000000) + " Million");
                    } else {
                        markettv.setText("Market Cap: " + d2);
                    }
                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return view;
        }
    }

}
