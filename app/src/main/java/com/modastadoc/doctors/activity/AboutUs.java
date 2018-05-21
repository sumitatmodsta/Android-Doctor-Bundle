package com.modastadoc.doctors.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.modastadoc.doctors.BuildConfig;
import com.modastadoc.doctors.R;
import com.modastadoc.doctors.common.constants.ServerConstants;
import com.modastadoc.doctors.widget.CustomProgressDialog;

/**
 * Created by ashutosh.kumar on 28/03/16.
 */

public class AboutUs extends AppCompatActivity {
    TextView version;
    private WebView webview;
    private static final String TAG = "AboutUs";
    //    private ProgressDialog progressBar;
    long startTime;
    AlertDialog alertDialog;
    CustomProgressDialog customDialog;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);

        context = this;
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about_modasta);
        }

        String versionName = BuildConfig.VERSION_NAME;
        version = (TextView) findViewById(R.id.version);
        version.setText("Version " + versionName);

        this.webview = (WebView) findViewById(R.id.webView);


        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        showCustomDialog();

        webview.setWebViewClient(new WebViewClient()
        {
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.i(TAG, "Processing webview url click...");
                view.loadUrl(url);
                return true;
            }

            public void onPageFinished(WebView view, String url)
            {
                Log.i(TAG, "Finished loading URL: " + url);
                dismissCustomDialog();
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                Log.e(TAG, "Error: " + description);
                dismissCustomDialog();
            }
        });


        webview.loadUrl(ServerConstants.BASE_URL + "/about-us/");
    }

    private void showCustomDialog()
    {

        customDialog = new CustomProgressDialog(AboutUs.this);
        customDialog.setCancelable(true);
        customDialog.show();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//        } else {
//            alertDialog = new ProgressDialog(this);
//            alertDialog.setCancelable(true);
//            alertDialog.setMessage("Loading...");
//            alertDialog.show();
//        }
    }

    private void dismissCustomDialog()
    {

        if (customDialog != null && customDialog.isShowing())
        {
            customDialog.dismiss();
        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // AppAnalytics.addAnalyticsInDB(context,"About Us page",Utils.activityStop(),"59","ash@gmail.com","25-05-88");
    }

}