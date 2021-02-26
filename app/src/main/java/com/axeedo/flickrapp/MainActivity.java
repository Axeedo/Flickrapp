package com.axeedo.flickrapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button for single image
        Button getImage = (Button)findViewById(R.id.btn_get_image);
        getImage.setOnClickListener(new GetImageOnClickListener());
        //Button for listactivity
        Button gotoList = (Button)findViewById(R.id.btn_goto_list);
        gotoList.setOnClickListener((new GotoListListener()));
    }

    //Button listeners and functions associated
    class GotoListListener implements  View.OnClickListener{
        @Override
        public void onClick(View v) {
            gotoList();
        }
    }
    private void gotoList(){
        Intent listActivity = new Intent(getApplicationContext(),ListActivity.class);
        startActivity(listActivity);
    }
    class GetImageOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view){
            getImageBtnClicked();
        }
    }
    private void getImageBtnClicked(){
        String url = getString(R.string.image_url);
        new AsyncFlickrJSONData().execute(url);
    }


    //AsyncTask for downloading JSON
    class AsyncFlickrJSONData extends AsyncTask<String, Void, JSONObject> {
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
            Log.i("JFL", json.toString());
            try {
                String link = json.getJSONArray("items").getJSONObject(1)
                        .getJSONObject("media").getString("m");
                new AsyncBitmapDownloader().execute(link);
            } catch (JSONException e) {
                Log.i("JFL", "JSONException", e);
            }
        }
    }

    //Asynctask for downloading the image
    class AsyncBitmapDownloader extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            Bitmap bm =  null;
            try{
                url = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    Log.i("JFL", "url: " + url);
                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    bm = BitmapFactory.decodeStream(in);
                    Log.i("JFL", "Bitmap: " + bm.toString());
                } finally {
                    conn.disconnect();
                }
            } catch (MalformedURLException e) {
                Log.i("JFL", "MalformedURLException", e);
            } catch (IOException e) {
                Log.i("JFL", "IOException", e);
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView result = (ImageView) findViewById(R.id.image);
            if (bitmap != null) {
                result.setImageBitmap(bitmap);
            }
        }
    }
}