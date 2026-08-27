package com.PouyaApp.kookyarkamancheh;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kookyar.common.PersianReshape;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tv1 = findViewById(R.id.aboutText);
        String str_tv = tv1.getText().toString();
        tv1.setText(PersianReshape.forDisplay(this, str_tv));

        TextView tv2 = findViewById(R.id.aboutText1);
        String str_tv2 = tv2.getText().toString();
        tv2.setText(PersianReshape.forDisplay(this, str_tv2));

        TextView tv3 = findViewById(R.id.aboutText2);
        String str_tv3 = tv3.getText().toString();
        tv3.setText(PersianReshape.forDisplay(this, str_tv3));

        TextView tv4 = findViewById(R.id.aboutText3);
        String str_tv4 = tv4.getText().toString();
        tv4.setText(PersianReshape.forDisplay(this, str_tv4));
    }
}
