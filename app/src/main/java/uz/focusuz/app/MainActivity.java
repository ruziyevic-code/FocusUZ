package uz.focusuz.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.graphics.Color;
import android.view.Window;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#101612"));
        window.setNavigationBarColor(Color.parseColor("#101612"));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#101612"));

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);

        setContentView(webView);

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
