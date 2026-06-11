package com.PouyaApp.kookyarkamancheh;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class StartScreen extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable launchRunnable = () -> {
        Intent tuner = new Intent(StartScreen.this, KamanchehTuner.class);
        startActivity(tuner);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_start_screen);

        setVersionInfo();
        handler.postDelayed(launchRunnable, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(launchRunnable);
    }

    private void setVersionInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            long versionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                    ? packageInfo.getLongVersionCode()
                    : packageInfo.versionCode;

            TextView versionNameTextView = findViewById(R.id.version_name);
            TextView versionCodeTextView = findViewById(R.id.version_code);

            if (versionNameTextView != null) versionNameTextView.setText(versionName);
            if (versionCodeTextView != null) versionCodeTextView.setText(String.valueOf(versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
