package com.hiddenpirates.msdbazarcom;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.hiddenpirates.msdbazarcom.classes.CustomFunctions;

public class MainActivity extends AppCompatActivity {

    private WebView webview;
    private String WEBSITE = "https://msdbazar.com";
    private RelativeLayout splashScreenLayout;
    private boolean doubleBackPressed = false;
    private RelativeLayout mainViewLayout;
    private final int UPDATE_REQUEST_CODE = 8348;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressBar spinner;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mainViewLayout = findViewById(R.id.mainView);
        splashScreenLayout = findViewById(R.id.splashScreenLayout);
        webview = findViewById(R.id.webView);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        spinner = findViewById(R.id.progressbar);

        webview.setWebViewClient(new CustomWebViewClient());

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);

        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

        webview.loadUrl(WEBSITE);

        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

//                Log.d("MY_TAG", String.valueOf(newProgress)+"\n");
//                Log.d("MY_TAG2", String.valueOf(webview.getProgress())+"\n");
//                newProgress value vul dichhe... hoot kore 100 hoyejache abar 0 ou hoye jachhe

                swipeRefreshLayout.setVisibility(View.INVISIBLE);

                if (webview.getProgress() == 100){
                    spinner.setVisibility(View.GONE);
                    swipeRefreshLayout.setVisibility(View.VISIBLE);
                }
                else{
                    spinner.setVisibility(View.VISIBLE);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> webview.reload());
//        -------------------------------------------------------

//--------------------------------------------------------------------------

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, UPDATE_REQUEST_CODE);
                }
                catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
//--------------------------------------------------------------------------

    }


    public class CustomWebViewClient extends WebViewClient{


        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage("SSL Error! Give Permission.");

            builder.setPositiveButton("Continue", (dialog, which) -> handler.proceed());

            builder.setNegativeButton("Cancel", (dialog, which) -> handler.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
             super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            splashScreenLayout.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);

            if (swipeRefreshLayout.isRefreshing()){
                swipeRefreshLayout.setRefreshing(false);
            }

            CustomFunctions.checkForUpdate(MainActivity.this);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            WEBSITE = view.getUrl();
            setContentView(R.layout.error);
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("MADARA", description);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            try {

                if (request.getUrl().toString().startsWith("tel:")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    startActivity(intent);
                }
                else if (request.getUrl().getHost().startsWith("wa.me")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    startActivity(intent);
                }
                else if (request.getUrl().getHost().startsWith("mailto:")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString()));
                    startActivity(intent);
                }
                else{
                    webview.loadUrl(request.getUrl().toString());
                }

            }
            catch (Exception e){
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

//----------------------------------------------------------------------------
/* Retry Loading the page */
    public void tryAgain(View v){
        recreate();
    }

//----------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        if(webview.canGoBack()) {
            webview.goBack();
        }
        else{
            if (doubleBackPressed){
                super.onBackPressed();
            }
            else {
                this.doubleBackPressed = true;
                Snackbar.make(mainViewLayout,"Double press to exit!",Snackbar.LENGTH_LONG).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackPressed = false, 2000);
            }
        }
    }
}