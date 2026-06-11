package com.PouyaApp.kookyarkamancheh;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kookyar.common.PersianReshape;

public class TunerHelp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner_help);

        TextView tunerHelp = findViewById(R.id.tunerHelp);
        if (tunerHelp != null) {
            String str = tunerHelp.getText().toString();
            tunerHelp.setText(PersianReshape.reshape(str));
        }
    }
}
