package com.PouyaApp.kookyarkamancheh;

import java.io.File;
import java.io.IOException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;




import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class KamanchehTuner extends Activity implements OnClickListener
		 {

	private static final String TAG = "Kuk yar";
	private PdUiDispatcher dispatcher;
	public String fonts = "BZar.ttf";
	private ImageView ivKamancheh;
	private Button Kamancheh1, Kamancheh2, Kamancheh3, Kamancheh4 ;
	private RadioButton miLaReSolRB,reLaReLaRB,miLaMiLaRB,reLaReSolRB;
	private TextView pitchLabel ;
	private PitchView pitchView;
	private Boolean kookSelected = false ;




	private double[] Miditone = new double[4];


	private PdService pdService = null;

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		blowUp.inflate(R.menu.kamancheh_menu, menu);
		return true;

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.help_tuner:
			Intent i = new Intent("android.intent.action.helpTunerKamancheh");
			startActivity(i);
			break;
		case R.id.bazgasht:
			finish();
			break;


		}
		return true;
	}
	
	private final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pdService = ((PdService.PdBinder) service).getService();
			try {
				initPd();
				loadPatch();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kamancheh_tuner);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initGui();
		initSystemServices();
		bindService(new Intent(this, PdService.class), pdConnection,
				BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(pdConnection);
	}

	private void initGui() {
			
	
		Typeface face = Typeface.createFromAsset(getAssets(), "font/" + fonts
				+ "");
		
		ivKamancheh = (ImageView) findViewById(R.id.ivkamancheh);
		Kamancheh1 = (Button) findViewById(R.id.kamancheh_1);
		Kamancheh1.setOnClickListener(this);
		Kamancheh1.setTypeface(face);
		String str_b1 = (String) Kamancheh1.getText().toString();
		Kamancheh1.setText(PersianReshape.reshape(str_b1));
		
		// -----------------------------------------------
		Kamancheh2 = (Button) findViewById(R.id.kamancheh_2);
		Kamancheh2.setOnClickListener(this);
		Kamancheh2.setTypeface(face);
		String str_b2 = (String) Kamancheh2.getText().toString();
		Kamancheh2.setText(PersianReshape.reshape(str_b2));

		// ------------------------------------------------
		Kamancheh3 = (Button) findViewById(R.id.kamancheh_3);
		Kamancheh3.setOnClickListener(this);
		Kamancheh3.setTypeface(face);
		String str_b3 = (String) Kamancheh3.getText().toString();
		Kamancheh3.setText(PersianReshape.reshape(str_b3));

		// ------------------------------------------------
		Kamancheh4 = (Button) findViewById(R.id.kamancheh_4);
		Kamancheh4.setOnClickListener(this);
		Kamancheh4.setTypeface(face);
		String str_b4 = (String) Kamancheh4.getText().toString();
		Kamancheh4.setText(PersianReshape.reshape(str_b4));


		// ------------------------------------------------
		pitchLabel = (TextView) findViewById(R.id.pitch_label);
		pitchView = (PitchView) findViewById(R.id.pitch_view);
		// ------------------------------------------------
		miLaReSolRB = (RadioButton) findViewById(R.id.MilaReSolRB) ;
		miLaReSolRB.setOnClickListener(this);
		miLaReSolRB.setTypeface(face);
		String str_rb1 = (String) miLaReSolRB.getText().toString();
		miLaReSolRB.setText(PersianReshape.reshape(str_rb1 ));
		// ------------------------------------------------
		reLaReLaRB = (RadioButton) findViewById(R.id.ReLaRelaRB) ;
		reLaReLaRB.setOnClickListener(this);
		reLaReLaRB.setTypeface(face);
		String str_rb2 = (String) reLaReLaRB.getText().toString();
		reLaReLaRB.setText(PersianReshape.reshape(str_rb2 ));
		// ------------------------------------------------
		miLaMiLaRB = (RadioButton) findViewById(R.id.MiLaMilaRB) ;
		miLaMiLaRB.setOnClickListener(this);
		miLaMiLaRB.setTypeface(face);
		String str_rb3 = (String) miLaMiLaRB.getText().toString();
		miLaMiLaRB.setText(PersianReshape.reshape(str_rb3 ));
		// ------------------------------------------------
		reLaReSolRB = (RadioButton) findViewById(R.id.ReLaReSolRB) ;
		reLaReSolRB.setOnClickListener(this);
		reLaReSolRB.setTypeface(face);
		String str_rb4 = (String) reLaReSolRB.getText().toString();
		reLaReSolRB.setText(PersianReshape.reshape(str_rb4 ));
		// ------------------------------------------------
		pitchLabel.setText(PersianReshape
				.reshape("سیم مورد نظر جهت کوک کردن را انتخاب کنید"));
	}

	private void initPd() throws IOException {
		// Configure the audio glue
		AudioParameters.init(this);
		int sampleRate = AudioParameters.suggestSampleRate();
		pdService.initAudio(sampleRate, 1, 2, 10.0f);
		start();

		// Create and install the dispatcher
		dispatcher = new PdUiDispatcher();
		PdBase.setReceiver(dispatcher);
		dispatcher.addListener("pitch", new PdListener.Adapter() {
			@Override
			public void receiveFloat(String source, final float x) {
				pitchView.setCurrentPitch(x);
			}
		});
	}

	private void start() {
		if (!pdService.isRunning()) {
			Intent intent = new Intent(KamanchehTuner.this, KamanchehTuner.class);
			pdService.startAudio(intent, R.drawable.ic_launcher,
					PersianReshape.reshape("کوک یار"),
					PersianReshape.reshape("بازگشت به کوک یار"));
		}
	}

	private void loadPatch() throws IOException {
		File dir = getFilesDir();
		IoUtils.extractZipResource(getResources().openRawResource(R.raw.tuner),
				dir, true);
		File patchFile = new File(dir, "tuner.pd");
		PdBase.openPatch(patchFile.getAbsolutePath());
	}

	private void initSystemServices() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (pdService == null)
					return;
				if (state == TelephonyManager.CALL_STATE_IDLE) {
					start();
				} else {
					pdService.stopAudio();
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void triggerNote(float n) {
		PdBase.sendFloat("midinote", n);
		PdBase.sendBang("trigger");

		pitchView.setCenterPitch(n);
	}

	@Override
	public void onClick(View v) {
			if(!kookSelected){
				Kamancheh1.setEnabled(true);
				Kamancheh2.setEnabled(true);
				Kamancheh3.setEnabled(true);
				Kamancheh4.setEnabled(true);
			}
			Kamancheh1.setBackgroundResource(android.R.drawable.btn_default);
			Kamancheh2.setBackgroundResource(android.R.drawable.btn_default);
			Kamancheh3.setBackgroundResource(android.R.drawable.btn_default);
			Kamancheh4.setBackgroundResource(android.R.drawable.btn_default);
			switch (v.getId()) {
			case R.id.kamancheh_1:
				triggerNote((float) Miditone[0]);
				pitchLabel.setText(PersianReshape.reshape("سیم اول"));
				ivKamancheh.setImageResource(R.drawable.kamancheh1);
				Kamancheh1.setBackgroundColor(Color.YELLOW);
				break;
			case R.id.kamancheh_2:
				triggerNote((float) Miditone[1]);
				pitchLabel.setText(PersianReshape.reshape("سیم دوم"));
				ivKamancheh.setImageResource(R.drawable.kamancheh2);
				
				Kamancheh2.setBackgroundColor(Color.YELLOW);
				break;
			case R.id.kamancheh_3:
				triggerNote((float) Miditone[2]);
				pitchLabel.setText(PersianReshape.reshape("سیم سوم"));
				ivKamancheh.setImageResource(R.drawable.kamancheh3);
				Kamancheh3.setBackgroundColor(Color.YELLOW);
				break;
			case R.id.kamancheh_4:
				triggerNote((float) Miditone[3]);
				pitchLabel.setText(PersianReshape.reshape("سیم چهارم"));
				ivKamancheh.setImageResource(R.drawable.kamancheh4);
				Kamancheh4.setBackgroundColor(Color.YELLOW);
				break;
			case R.id.MilaReSolRB :
				double[] MilaReSolRBMiditone ={74,67,60,53} ;
				Miditone = MilaReSolRBMiditone ;
				
			//	miLaReSolRB.setChecked(false);
				reLaReLaRB.setChecked(false);;
				miLaMiLaRB.setChecked(false);;
				reLaReSolRB.setChecked(false);
				kookSelected=true ;
				break;
			case R.id.ReLaRelaRB :
				double[] ReLaRelaRBMiditone ={72,67,60,55} ;
				Miditone = ReLaRelaRBMiditone ;
				miLaReSolRB.setChecked(false);
			//	reLaReLaRB.setChecked(false);
				miLaMiLaRB.setChecked(false);
				reLaReSolRB.setChecked(false);
				kookSelected=true ;
				break;
			case R.id.MiLaMilaRB:
				double[] MiLaMilaRBMiditone ={74,67,62,55} ;
				Miditone = MiLaMilaRBMiditone ;
				miLaReSolRB.setChecked(false);
				reLaReLaRB.setChecked(false);
			//	miLaMiLaRB.setChecked(false);
				reLaReSolRB.setChecked(false);
				kookSelected=true ;
				break;

			case R.id.ReLaReSolRB:
				double[] ReLaReSolRBMiditone ={72,67,60,53} ;
				Miditone = ReLaReSolRBMiditone ;
				miLaReSolRB.setChecked(false);
				reLaReLaRB.setChecked(false);
				miLaMiLaRB.setChecked(false);
		//		reLaReSolRB.setChecked(false);
				kookSelected=true ;
				break ;
			}
		
		
		

	}


}