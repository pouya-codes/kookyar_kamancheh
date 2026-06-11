package com.PouyaApp.kookyarkamancheh;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.kookyar.common.PersianReshape;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_ALL = 1;
    private static final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    Button tunerB, exitB, aboutB, helpB;
    Toast exitToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setFace();
    }

    private boolean hasPermissions(String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void setFace() {
        tunerB = (Button) findViewById(R.id.TunerButton);
        tunerB.setOnClickListener(this);

        exitB = (Button) findViewById(R.id.exitButton);
        exitB.setOnClickListener(this);

        aboutB = (Button) findViewById(R.id.about);
        aboutB.setOnClickListener(this);

        helpB = (Button) findViewById(R.id.rahnama);
        helpB.setOnClickListener(this);
    }

    @Override
    public void onClick(View arg0) {
        Intent activity;
        int viewId = arg0.getId();

        if (viewId == R.id.TunerButton) {
            if (!hasPermissions(PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                activity = new Intent(MainMenu.this, KamanchehTuner.class);
                startActivity(activity);
            }
        } else if (viewId == R.id.rahnama) {
            activity = new Intent(MainMenu.this, Help.class);
            startActivity(activity);
        } else if (viewId == R.id.about) {
            activity = new Intent(MainMenu.this, About.class);
            startActivity(activity);
        } else if (viewId == R.id.exitButton) {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent activity = new Intent(MainMenu.this, KamanchehTuner.class);
                startActivity(activity);
            } else {
                Toast.makeText(this, "دسترسی به میکروفون برای کار کردن کوک یار ضروری است", Toast.LENGTH_LONG).show();
            }
        }
    }
}
