package com.PouyaApp.kookyarkamancheh;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kookyar.common.PersianReshape;

public class Help extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView help = findViewById(R.id.helpTV);
        String str_tv = help.getText().toString();
        help.setText(PersianReshape.reshape(str_tv));
    }
}
