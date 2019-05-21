package com.example.visionterm_01;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class VrPanoramaActivity extends Activity {

    private VrPanoramaView panoview;
    private Bitmap bitmap;
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
    Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == 0){
                setPanoview();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr);

        Intent intent = getIntent();
        final String currentUrl = intent.getExtras().getString("url");

        setBitmap(currentUrl);
    }

    public void setBitmap(String s){
        final String s2 = s;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
/*
                    URL curUrl = new URL(s2);
                    URLConnection conn = curUrl.openConnection();
                    conn.connect();

                    int nSize = conn.getContentLength();
                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), nSize);
*/

                    URL url = new URL(s2);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(input);/*

                    bitmap = BitmapFactory.decodeStream(bis);
                    bis.close();*/
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        while(thread.getState() != Thread.State.TERMINATED){
            if(thread.getState() == Thread.State.TERMINATED){
                handler.sendEmptyMessage(0);
                break;
            }
        }
    }

    public void setPanoview(){
        panoview = (VrPanoramaView) findViewById(R.id.pano_view);
        panoview.setEventListener(new VrPanoramaActivity.ActivityEventListener());

        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
        panoview.loadImageFromBitmap(bitmap, panoOptions);
        panoview.setDisplayMode(3);
    }

    @Override
    protected void onPause() {
        panoview.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoview.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoview.shutdown();
        super.onDestroy();
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            //loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            //loadImageSuccessful = false;
            Toast.makeText(
                    VrPanoramaActivity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG).show();
            Log.e("TAG", "Error loading pano: " + errorMessage);
        }
    }
}
