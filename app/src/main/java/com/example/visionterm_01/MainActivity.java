package com.example.visionterm_01;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private Button btn;

    private VrPanoramaView panoview;
    private Bitmap bitmap;
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "http://maps.google.com";

        webView = (WebView) findViewById(R.id.webview);
        btn = (Button)findViewById(R.id.btn);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClientClass());

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUrl = webView.getUrl();
                try {
                    URL curUrl = new URL(currentUrl);
                    bitmap = BitmapFactory.decodeStream(curUrl.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                panoview = (VrPanoramaView) findViewById(R.id.pano_view);
                panoview.setEventListener(new ActivityEventListener());

                panoOptions.inputType = VrPanoramaView.Options.TYPE_STEREO_OVER_UNDER;
                panoview.loadImageFromBitmap(bitmap, panoOptions);
                panoview.setDisplayMode(3);
            }
        });
    }

    private class WebViewClientClass extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {//뒤로가기 버튼 이벤트
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {//웹뷰에서 뒤로가기 버튼을 누르면 뒤로가짐
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
                   MainActivity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG).show();
            Log.e("TAG", "Error loading pano: " + errorMessage);
        }
    }
}
