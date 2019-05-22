package com.example.visionterm_01;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class VrPanoramaActivity extends Activity {

    private VrPanoramaView panoview;
    private String key = "AIzaSyAefKkewKHIg69-pulc0QP3Jwg7PdibN4s";
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr);

        Intent intent = getIntent();
        final String currentUrl = intent.getExtras().getString("url");

        setBitmap(currentUrl);
    }

    public String coordinate(String s){
        String x, y, result;
        String str = s;

        int index = str.indexOf("@");
        String substr = str.substring(index+1);
        String data[] = substr.split(",");
        y = data[0];
        x = data[1];

        result = y + "," + x;

        return result;
    }

    public void setBitmap(String s){
        String s2 = s;
        String coor = coordinate(s2);
        System.out.println("coor : " + coor);
        final String url = "https://maps.googleapis.com/maps/api/streetview?size=600x300&location="+coor+"&fov=60&key="+key;
        System.out.println("pano url : " + url);

        Thread thread = new Thread(){
            public void run(){
                try {
                    InputStream in = new URL(url).openStream();
                    Bitmap tmp = BitmapFactory.decodeStream(in);
                    bitmap = makePano(tmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        while(thread.getState() != Thread.State.TERMINATED){
            if(thread.getState() == Thread.State.TERMINATED){
                break;

            }
        }

        setPanoview(bitmap);
    }

    public void setPanoview(Bitmap bit){
        panoview = (VrPanoramaView) findViewById(R.id.pano_view);
        panoview.setEventListener(new VrPanoramaActivity.ActivityEventListener());

        panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
        panoview.loadImageFromBitmap(bit, panoOptions);
        panoview.setDisplayMode(3);
    }

    public Bitmap makePano(Bitmap bit){
        Bitmap result = null;
        Bitmap tmp = Bitmap.createBitmap(bit).copy(Bitmap.Config.ARGB_8888, true);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPurgeable = true;

        result = Bitmap.createScaledBitmap(bit, bit.getWidth(), bit.getHeight() + bit.getHeight(), true);

        Paint p = new Paint();
        p.setDither(true);
        p.setFlags(Paint.ANTI_ALIAS_FLAG);

        Canvas c = new Canvas(result);
        c.drawBitmap(bit, 0, 0, p);

        c.drawBitmap(tmp, 0, bit.getHeight(), p);

        return result;
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
