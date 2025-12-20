package com.PouyaApp.kookyarkamancheh;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Help extends Activity {
	public String fonts="BZar.ttf";
	private TextView help ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		Typeface face = Typeface.createFromAsset(getAssets(), "font/"+fonts+"");
		help = (TextView) findViewById(R.id.helpTV) ;
		help.setTypeface(face);
		String str_tv = (String) help.getText().toString();
		help.setText(PersianReshape.reshape(str_tv));

	}
}
