package com.PouyaApp.kookyarkamancheh;




import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainMenu extends Activity implements View.OnClickListener{

	Button tunerB,exitB,aboutB,helpB ;
	public String fonts="BZar.ttf";
    Toast       exitToast;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu) ;
		setFace();
        exitToast = Toast.makeText(getApplicationContext(), "برای خروج، کلید بازگشت را دوباره بزنید",
                Toast.LENGTH_SHORT);

        
        
		}
	protected void setFace() {
		
		tunerB = (Button) findViewById(R.id.TunerButton);
		tunerB.setOnClickListener(this) ;
		

		exitB = (Button) findViewById(R.id.exitButton);
		exitB.setOnClickListener(this) ;
		
		aboutB = (Button) findViewById(R.id.about) ;
		aboutB.setOnClickListener(this);
		helpB = (Button) findViewById(R.id.rahnama) ;
		helpB.setOnClickListener(this);
		Typeface face = Typeface.createFromAsset(getAssets(), "font/"+fonts+"");
		tunerB.setTypeface(face);
        String str_tuner = (String) tunerB.getText().toString();
        tunerB.setText(PersianReshape.reshape(str_tuner));
        
        exitB.setTypeface(face);
        String str_exit = (String) tunerB.getText().toString();
        tunerB.setText(PersianReshape.reshape(str_exit)); 
        
        aboutB.setTypeface(face);
        String str_about= (String) aboutB.getText().toString();
        aboutB.setText(PersianReshape.reshape(str_about)); 
        
        helpB.setTypeface(face);
        String str_help = (String) helpB.getText().toString();
        helpB.setText(PersianReshape.reshape(str_help)); 
        
        

	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent activity ;
		
		switch (arg0.getId()) {
		
		case R.id.TunerButton:{
			activity = new Intent("android.intent.action.KamanchehTuner") ;
			startActivity(activity);
			break ;
		}
		case R.id.rahnama:{
			
	
				activity = new Intent("android.intent.action.helpKamancheh");
			startActivity(activity);
			break ;
		}

		case R.id.about:{
			
			activity = new Intent("android.intent.action.AboutKamancheh");
		
			startActivity(activity);
			break ;
		}
		
		case R.id.exitButton:{
			finish();
			break ;
		}
		}

		
	}
    @Override
    public void onBackPressed() {
        if (exitToast.getView().isShown())
        {
            exitToast.cancel();
            finish();
        }
        else
        {
            exitToast.show();
        }
    
}





}
