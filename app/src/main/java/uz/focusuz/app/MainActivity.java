package uz.focusuz.app;

import android.app.Activity;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import java.util.Calendar;
import java.util.ArrayList;

import android.os.Bundle;
import android.webkit.WebView;
import android.graphics.Color;
import android.view.Window;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen splash =
    androidx.core.splashscreen.SplashScreen.installSplashScreen(this);

    super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                    new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS
                    },
                    301
                );
            }
        }


    splash.setOnExitAnimationListener(provider -> {
        android.view.View view = provider.getView();
        view.animate()
            .translationY(-view.getHeight())
            .alpha(0f)
            .setDuration(350)
            .setInterpolator(
                new android.view.animation.DecelerateInterpolator()
            )
            .withEndAction(provider::remove)
            .start();
    });

        Window window = getWindow();
        window.setStatusBarColor(Color.parseColor("#101612"));
        window.setNavigationBarColor(Color.parseColor("#101612"));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#101612"));

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);

        webView.setOverScrollMode(android.view.View.OVER_SCROLL_NEVER);

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false);
            window.getInsetsController().hide(
                android.view.WindowInsets.Type.systemBars()
            );
            window.getInsetsController().setSystemBarsBehavior(
                android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        } else {
            window.getDecorView().setSystemUiVisibility(5894);
        }

        setContentView(webView);

        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new StatsBridge(), "FocusAndroid");
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    public class StatsBridge {
        @android.webkit.JavascriptInterface
        public String getAppInfo(String pkg) {
            try {
                android.content.pm.PackageManager pm =
                    getPackageManager();

                android.content.pm.ApplicationInfo info =
                    pm.getApplicationInfo(pkg, 0);

                String name =
                    pm.getApplicationLabel(info).toString();

                android.graphics.drawable.Drawable icon =
                    pm.getApplicationIcon(info);

                android.graphics.Bitmap bitmap =
                    android.graphics.Bitmap.createBitmap(
                        96, 96,
                        android.graphics.Bitmap.Config.ARGB_8888
                    );

                android.graphics.Canvas canvas =
                    new android.graphics.Canvas(bitmap);

                icon.setBounds(0, 0, 96, 96);
                icon.draw(canvas);

                java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();

                bitmap.compress(
                    android.graphics.Bitmap.CompressFormat.PNG,
                    100, out
                );

                String base64 =
                    android.util.Base64.encodeToString(
                        out.toByteArray(),
                        android.util.Base64.NO_WRAP
                    );

                org.json.JSONObject result =
                    new org.json.JSONObject();

                result.put("name", name);
                result.put("icon",
                    "data:image/png;base64," + base64);

                return result.toString();

            } catch (Exception e) {
                return "{}";
            }
        }


        @JavascriptInterface
        public void finishAlert(boolean sound, boolean vibration) {
            runOnUiThread(() -> {
                if (sound) {
                    try {
                        android.media.Ringtone ringtone =
                            android.media.RingtoneManager.getRingtone(
                                MainActivity.this,
                                android.media.RingtoneManager.getDefaultUri(
                                    android.media.RingtoneManager.TYPE_NOTIFICATION
                                )
                            );
                        if (ringtone != null) ringtone.play();
                    } catch (Exception ignored) {}
                }

                if (vibration) {
                    try {
                        android.os.Vibrator v =
                            (android.os.Vibrator)
                            getSystemService(Context.VIBRATOR_SERVICE);

                        if (v != null && v.hasVibrator()) {
                            v.vibrate(
                                android.os.VibrationEffect.createOneShot(
                                    400,
                                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                                )
                            );
                        }
                    } catch (Exception ignored) {}
                }
            });
        }


        @JavascriptInterface
        public boolean hasPermission() {
            AppOpsManager manager = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);

            return manager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getPackageName()
            ) == AppOpsManager.MODE_ALLOWED;
        }

        @JavascriptInterface
        public void openPermission() {
            runOnUiThread(() -> {
                startActivity(new Intent(
                    Settings.ACTION_USAGE_ACCESS_SETTINGS
                ));
            });
        }

        @JavascriptInterface
        public String getStats(int days) {
            JSONArray result = new JSONArray();

            if (!hasPermission()) return result.toString();

            try {
                days = Math.max(1, Math.min(30, days));

                Calendar start = Calendar.getInstance();
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                start.add(Calendar.DAY_OF_YEAR, 1-days);

                UsageStatsManager manager =
                    (UsageStatsManager)
                    getSystemService(Context.USAGE_STATS_SERVICE);

                Map<String, UsageStats> data =
                    manager.queryAndAggregateUsageStats(
                        start.getTimeInMillis(),
                        System.currentTimeMillis()
                    );

                ArrayList<UsageStats> apps =
                    new ArrayList<>(data.values());

                apps.sort((a,b) -> Long.compare(
                    b.getTotalTimeInForeground(),
                    a.getTotalTimeInForeground()
                ));

                for (UsageStats app : apps) {
                    long time = app.getTotalTimeInForeground();
                    if (time < 1000) continue;

                    String pkg = app.getPackageName();
                    String name = pkg;

                    try {
                        name = getPackageManager()
                            .getApplicationLabel(
                                getPackageManager()
                                .getApplicationInfo(pkg, 0)
                            ).toString();
                    } catch (Exception ignored) {}

                    JSONObject item = new JSONObject();
                    item.put("name", name);
                    item.put("package", pkg);
                    item.put("seconds", time / 1000);
                    result.put(item);
                }

            } catch (Exception ignored) {}

            return result.toString();
        }
    }

}
