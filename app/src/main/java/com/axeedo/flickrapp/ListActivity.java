package com.axeedo.flickrapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class ListActivity extends AppCompatActivity {

    private RequestQueue queue;
    private final Object stopRequestTag = new Object();
    //private Context context = this.getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ListView list = findViewById(R.id.list);

        MyAdapter adapter = new MyAdapter();
        list.setAdapter(adapter);

        String url = getString(R.string.image_url);
        new AsyncFlickrJSONDataForList(adapter).execute(url);
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(stopRequestTag);
        }
    }

    // Adapter linked to activity list
    class MyAdapter extends BaseAdapter{
        private Vector<String> urls;
        MyAdapter(){
            urls = new Vector<>();
        }
        public void add(String url){
            urls.add(url);
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public Object getItem(int position) {
            return urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //Image list
            RequestQueue queue = MySingleton.getInstance(parent.getContext()).
                    getRequestQueue();
            //Alternatively, we can use Volley's pre-made queue:
            // RequestQueue requestQueue = Volley.newRequestQueue(parent.getContext())

            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.bitmaplayout, parent, false);
            }

            View finalV = convertView;
            ImageRequest imageRequest = new ImageRequest(
                    (String)getItem(position),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            ((ImageView)finalV.findViewById(R.id.itemImage)).setImageBitmap(response);
                        }
                    }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ((ImageView)finalV.findViewById(R.id.itemImage)).setImageResource(R.drawable.ic_launcher_background);
                        }
                    }
            );
            imageRequest.setTag(stopRequestTag);
            queue.add(imageRequest);
            return finalV;
        }
    }

    class AsyncFlickrJSONDataForList extends AsyncTask<String, Void, JSONObject> {
        MyAdapter adapter;
        public AsyncFlickrJSONDataForList(MyAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            URL url;
            JSONObject json =  null;
            try{
                url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    String s = readStream(in);
                    json = new JSONObject(s);
                } catch (JSONException e) {
                    Log.i("JFL", "JSONException", e);
                } finally {
                    conn.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.i("JFL", "MalformedURLException", e);
            } catch (IOException e) {
                Log.i("JFL", "IOException", e);
            }
            return json;
        }

        private String readStream(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            try {
                for (String line = reader.readLine(); line != null; line =reader.readLine()){
                    sb.append(line);
                }
            } catch (IOException e) {
                Log.i("JFL", "IOException", e);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i("JFL", "IOException", e);
                }
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                int size = json.getJSONArray("items").length();
                Log.i("JFL", "Size of Array: " + size);
                for(int i=0; i<size; i++){
                    String url = json.getJSONArray("items").getJSONObject(i)
                            .getJSONObject("media").getString("m");
                    adapter.add(url);

                    Log.i("JFL", "Adding to adapter url : " + url);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.i("JFL", "JSONException", e);
            }
        }
    }
}

class MySingleton {
    private static MySingleton instance;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    private static Context ctx;

    private MySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();

        imageLoader = new ImageLoader(requestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new MySingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}