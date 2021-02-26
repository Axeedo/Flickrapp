package com.axeedo.flickrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
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
import java.util.Queue;
import java.util.Vector;

public class ListActivity extends AppCompatActivity {

    private RequestQueue queue;
    private final Object tag = new Object();
    //private Context context = this.getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ListView list = (ListView)findViewById(R.id.list);

        MyAdapter adapter = new MyAdapter();
        list.setAdapter(adapter);

        String url = getString(R.string.image_url);
        new AsyncFlickrJSONDataForList(adapter).execute(url);


    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(tag);
        }
    }

    /*protected void makeRequest(){
        RequestQueue requestQueue; //= MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();
    }*/

    // Adapter linked to activity list
    class MyAdapter extends BaseAdapter{
        private Vector<String> urls;
        MyAdapter(){
            urls = new Vector<String>();
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
            Log.i("JFL", "TODO");
            
            //JSON text list
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.textviewlayout, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.textviewlayout)).setText((String)getItem(position));

            return convertView;

            //Image list (Nullpointer exception)
            /*RequestQueue queue = MySingleton.getInstance(parent.getContext()).
                    getRequestQueue();
            //ImageView v = (ImageView)View.inflate(context,R.layout.bitmaplayout,null);
            View v = getLayoutInflater().inflate(R.layout.bitmaplayout, parent, false);
            //NetworkImageView imageView  = (NetworkImageView) v.findViewById(R.id.image);
            ImageRequest imageRequest = new ImageRequest(
                    "http://img.my.csdn.net/uploads/201603/26/1458988468_5804.jpg",
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            v.setImageBitmap(response);
                        }
                    }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    v.setImageResource(R.drawable.ic_launcher_background);
                }});
            imageRequest.setTag(tag);
            queue.add(imageRequest);
            return v;*/



            /*Response.Listener<Bitmap> rep_listener = response -> {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.bitmaplayout, parent, false);
                }
                ImageView result = (ImageView) findViewById(R.id.image);
                if (bitmap != null) {
                    result.setImageBitmap(bitmap);
                }
            }
            /*for(String url : urls){
                ImageRequest imageRequest = new ImageRequest(url, );
            }*/

        }
    }

    /*private void getImages(String url, ImageView imageView){

        /*imageLoader.get(url, ImageLoader.getImageListener(imageView,
                R.drawable.image, android.R.drawable
                        .ic_dialog_alert));
        //imageView.setImageUrl(url, imageLoader);
    }*/

    class AsyncFlickrJSONDataForList extends AsyncTask<String, Void, JSONObject> {
        MyAdapter adapter;
        public AsyncFlickrJSONDataForList(MyAdapter adapter){
            this.adapter = adapter;
        }
        @Override
        protected JSONObject doInBackground(String... strings) {
            URL url = null;
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
                //new ListActivity.AsyncBitmapDownloader().execute(link);
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
            /*Cache cache = new DiskBasedCache(ctx.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = new RequestQueue(cache, network);*/
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