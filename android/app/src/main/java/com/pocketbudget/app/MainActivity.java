package com.pocketbudget.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Pocket Budget: shows the budget web app full screen.
 * The app marks itself with "PocketBudgetApp" in its user agent, which is what lets
 * this phone create and edit its own budget. Anyone else only gets the view-only page.
 */
public class MainActivity extends Activity {

    private WebView web;
    private String appUrl;

    private static final String OFFLINE_PAGE =
        "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
        "<body style='margin:0;font-family:sans-serif;background:#EEF1F4;color:#15212C;display:flex;" +
        "align-items:center;justify-content:center;height:100vh;text-align:center;padding:24px;box-sizing:border-box'>" +
        "<div><h2 style='margin:0 0 8px'>No internet connection</h2>" +
        "<p style='color:#5D6B78;margin:0 0 20px'>Pocket Budget needs the internet to save and sync your budget.</p>" +
        "<button onclick='Android.retry()' style='background:#1F5F8B;color:#fff;border:0;border-radius:10px;" +
        "padding:12px 22px;font-size:16px;font-weight:bold'>Try again</button></div></body></html>";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appUrl = getString(R.string.app_url);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#EEF1F4"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(s.getUserAgentString() + " PocketBudgetApp/1.0");
        CookieManager.getInstance().setAcceptCookie(true);

        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                Uri home = Uri.parse(appUrl);
                // Stay inside the app for our own site; open everything else in the phone's browser.
                if (uri.getHost() != null && uri.getHost().equals(home.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadDataWithBaseURL(null, OFFLINE_PAGE, "text/html", "utf-8", null);
                }
            }
        });

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl(appUrl);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }

    /** Functions the web page can call as window.Android.* */
    private class Bridge {
        @JavascriptInterface
        public void share(String text) {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, text);
            runOnUiThread(() -> startActivity(Intent.createChooser(send, "Share your budget")));
        }

        @JavascriptInterface
        public void retry() {
            runOnUiThread(() -> web.loadUrl(appUrl));
        }
    }
}
