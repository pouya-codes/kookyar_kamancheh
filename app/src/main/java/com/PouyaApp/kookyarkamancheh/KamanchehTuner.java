package com.PouyaApp.kookyarkamancheh;

import java.io.File;
import java.io.IOException;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.kookyar.common.PersianReshape;
import com.kookyar.common.PitchView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class KamanchehTuner extends AppCompatActivity implements OnClickListener, OnItemSelectedListener {

    private static final String TAG = "KookYaR";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1001;

    final private int highSensitive = 55;
    final private int midSensitive = 50;
    final private int lowSensitive = 45;

    private PdUiDispatcher dispatcher;
    public String fonts = "BZar.ttf";
    private ImageView ivKamancheh;
    private Button Kamancheh1, Kamancheh2, Kamancheh3, Kamancheh4;
    private RadioButton miLaReSolRB, reLaReLaRB, miLaMiLaRB, reLaReSolRB;
    private TextView pitchLabel;
    private PitchView pitchView;
    private CheckBox tuning;
    private Spinner kookChangeSpinner;
    private Boolean kookSelected = false;
    OnSharedPreferenceChangeListener listener;
    SharedPreferences getprefs;
    private int laFrequens = 440;
    private int sensitiveLevel = midSensitive;
    private int pressure;
    private double nimpardehSub = 0;
    private int selectedStringIndex = -1;
    private int lastTunedIndex = -1;
    private final Handler pitchHandler = new Handler(Looper.getMainLooper());
    private Runnable pitchResetRunnable;

    private double[] Miditone = new double[4];

    private PdService pdService = null;

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater blowUp = getMenuInflater();
        blowUp.inflate(R.menu.kamancheh_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.help_tuner) {
            Intent i = new Intent(KamanchehTuner.this, TunerHelp.class);
            startActivity(i);
        } else if (id == R.id.bazgasht) {
            finish();
        } else if (id == R.id.settings_menu) {
            Intent j = new Intent(KamanchehTuner.this, Prefs.class);
            startActivity(j);
        }
        return true;
    }

    private void prefsListnner() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                laFrequens = Integer.parseInt(prefs.getString("ref", "440"));
                pitchView.setAFrequnse(laFrequens);

                if (prefs.getString("sensitive", "2").compareTo("3") == 0) {
                    sensitiveLevel = highSensitive;
                } else if (prefs.getString("sensitive", "2").compareTo("1") == 0) {
                    sensitiveLevel = midSensitive;
                } else {
                    sensitiveLevel = lowSensitive;
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
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
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kamancheh_tuner);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initGui();
        prefsListnner();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
        } else {
            initAudio();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initAudio();
            } else {
                Toast.makeText(this, "دسترسی به میکروفون برای کار کردن کوک یار ضروری است", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initAudio() {
        bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pitchView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pitchView.resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pitchHandler.removeCallbacksAndMessages(null);
        try {
            unbindService(pdConnection);
        } catch (Exception e) {
            // ignore if not bound
        }
    }

    private void initGui() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        Typeface face = Typeface.createFromAsset(getAssets(), "font/" + fonts);

        ivKamancheh = (ImageView) findViewById(R.id.ivkamancheh);
        Kamancheh1 = (Button) findViewById(R.id.kamancheh_1);
        Kamancheh1.setOnClickListener(this);
        Kamancheh1.setTypeface(face);
        Kamancheh1.setText(PersianReshape.reshape(Kamancheh1.getText().toString()));

        Kamancheh2 = (Button) findViewById(R.id.kamancheh_2);
        Kamancheh2.setOnClickListener(this);
        Kamancheh2.setTypeface(face);
        Kamancheh2.setText(PersianReshape.reshape(Kamancheh2.getText().toString()));

        Kamancheh3 = (Button) findViewById(R.id.kamancheh_3);
        Kamancheh3.setOnClickListener(this);
        Kamancheh3.setTypeface(face);
        Kamancheh3.setText(PersianReshape.reshape(Kamancheh3.getText().toString()));

        Kamancheh4 = (Button) findViewById(R.id.kamancheh_4);
        Kamancheh4.setOnClickListener(this);
        Kamancheh4.setTypeface(face);
        Kamancheh4.setText(PersianReshape.reshape(Kamancheh4.getText().toString()));

        pitchLabel = (TextView) findViewById(R.id.pitch_label);
        pitchView = (PitchView) findViewById(R.id.pitch_view);

        miLaReSolRB = (RadioButton) findViewById(R.id.MilaReSolRB);
        miLaReSolRB.setOnClickListener(this);
        miLaReSolRB.setTypeface(face);
        miLaReSolRB.setText(PersianReshape.reshape(miLaReSolRB.getText().toString()));

        reLaReLaRB = (RadioButton) findViewById(R.id.ReLaRelaRB);
        reLaReLaRB.setOnClickListener(this);
        reLaReLaRB.setTypeface(face);
        reLaReLaRB.setText(PersianReshape.reshape(reLaReLaRB.getText().toString()));

        miLaMiLaRB = (RadioButton) findViewById(R.id.MiLaMilaRB);
        miLaMiLaRB.setOnClickListener(this);
        miLaMiLaRB.setTypeface(face);
        miLaMiLaRB.setText(PersianReshape.reshape(miLaMiLaRB.getText().toString()));

        reLaReSolRB = (RadioButton) findViewById(R.id.ReLaReSolRB);
        reLaReSolRB.setOnClickListener(this);
        reLaReSolRB.setTypeface(face);
        reLaReSolRB.setText(PersianReshape.reshape(reLaReSolRB.getText().toString()));

        getprefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        laFrequens = Integer.parseInt(getprefs.getString("ref", "440"));
        pitchView.setAFrequnse(laFrequens);

        if (getprefs.getString("sensitive", "2").compareTo("3") == 0) {
            sensitiveLevel = highSensitive;
        } else if (getprefs.getString("sensitive", "2").compareTo("1") == 0) {
            sensitiveLevel = midSensitive;
        } else {
            sensitiveLevel = lowSensitive;
        }

        kookChangeSpinner = (Spinner) findViewById(R.id.spinnerKookChange);
        ArrayAdapter<CharSequence> kookChangeAdapter = ArrayAdapter.createFromResource(this, R.array.kookChange, R.layout.spinner_item);
        kookChangeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kookChangeSpinner.setAdapter(kookChangeAdapter);
        kookChangeSpinner.setOnItemSelectedListener(this);
        kookChangeSpinner.setSelection(getprefs.getInt("change", 0));

        tuning = (CheckBox) findViewById(R.id.checkBox_tuning);
        findViewById(R.id.tuning).setOnClickListener(this);
        tuning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lastTunedIndex = -1;
                if (isChecked) {
                    Toast.makeText(getApplicationContext(),
                            "توجه : در صورتی که اولین بار است ساز خود را با کوک یار کوک می کنید، بهتر است سیم ها را به صورت دستی انتخاب کنید",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        pitchLabel.setText(PersianReshape.reshape("سیم مورد نظر جهت کوک کردن را انتخاب کنید"));

        resetStringButtonTints();
    }

    private void resetStringButtonTints() {
        int defaultTint = Color.parseColor("#FF303F9F");
        Kamancheh1.setBackgroundTintList(ColorStateList.valueOf(defaultTint));
        Kamancheh2.setBackgroundTintList(ColorStateList.valueOf(defaultTint));
        Kamancheh3.setBackgroundTintList(ColorStateList.valueOf(defaultTint));
        Kamancheh4.setBackgroundTintList(ColorStateList.valueOf(defaultTint));
    }

    private void selectStringButton(Button selected) {
        resetStringButtonTints();
        selected.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFBB33")));
    }

    private void initPd() throws IOException {
        AudioParameters.init(this);
        int sampleRate = AudioParameters.suggestSampleRate();
        pdService.initAudio(sampleRate, 1, 2, 20.0f);
        start();

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);
        dispatcher.addListener("pitch", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, final float x) {
                if (pressure >= sensitiveLevel) {
                    if (pitchResetRunnable != null) {
                        pitchHandler.removeCallbacks(pitchResetRunnable);
                    }
                    float pitch = (float) Math.round(x * 1000) / 1000;
                    pitchView.setCurrentPitch(pitch);

                    if (tuning.isChecked() && kookSelected) {
                        for (int i = 0; i < Miditone.length; i++) {
                            double target = Miditone[i] - nimpardehSub;
                            if (pitch >= target - 0.4 && pitch <= target + 0.4) {
                                autoTune(i);
                                break;
                            }
                        }
                    }

                    pitchResetRunnable = () -> pitchView.setCurrentPitch(12);
                    pitchHandler.postDelayed(pitchResetRunnable, 2500);
                }
            }
        });

        dispatcher.addListener("pressure", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, final float z) {
                pressure = (int) z;
            }
        });
    }

    private void start() {
        if (!pdService.isRunning()) {
            pdService.startAudio();
        }
    }

    private void loadPatch() throws IOException {
        File dir = getFilesDir();
        IoUtils.extractZipResource(getResources().openRawResource(R.raw.temp),
                dir, true);
        File patchFile = new File(dir, "path");
        PdBase.openPatch(patchFile.getAbsolutePath());
    }

    private void triggerNote(float n) {
        float realMidi = midiToRealMidi(n);
        pitchView.setCenterPitch(realMidi);
        pitchView.setCurrentPitch(12);
        pitchView.setMidiRef(n);
        PdBase.sendFloat("midinote", realMidi);
        PdBase.sendBang("trigger");
    }

    private void triggerNote2(float n) {
        float realMidi = midiToRealMidi(n);
        pitchView.setCenterPitch(realMidi);
        pitchView.setMidiRef(n);
    }

    private float midiToRealMidi(float midi) {
        double frequens = laFrequens * Math.pow(2, ((midi - 69) / 12));
        float RealMidi = 69 + 12 * (float) Math.log(frequens / 440.0f)
                / (float) Math.log(2.0);
        RealMidi = (float) Math.round(RealMidi * 1000) / 1000;
        return RealMidi;
    }

    private void selectString(int i, int imageRes, String label) {
        selectedStringIndex = i;
        lastTunedIndex = -1;
        triggerNote((float) (Miditone[i] - nimpardehSub));
        pitchLabel.setText(PersianReshape.reshape(label));
        ivKamancheh.setImageResource(imageRes);
        YoYo.with(Techniques.Landing).duration(700).playOn(ivKamancheh);
        Button[] btns = {Kamancheh1, Kamancheh2, Kamancheh3, Kamancheh4};
        selectStringButton(btns[i]);
    }

    private void autoTune(int i) {
        if (i == lastTunedIndex) return;
        lastTunedIndex = i;
        selectedStringIndex = i;
        int[] imgs = {R.drawable.kamancheh1, R.drawable.kamancheh2, R.drawable.kamancheh3, R.drawable.kamancheh4};
        String[] labels = {"\u0633\u06CC\u0645 \u0627\u0648\u0644", "\u0633\u06CC\u0645 \u062F\u0648\u0645", "\u0633\u06CC\u0645 \u0633\u0648\u0645", "\u0633\u06CC\u0645 \u0686\u0647\u0627\u0631\u0645"};
        triggerNote2((float) (Miditone[i] - nimpardehSub));
        pitchLabel.setText(PersianReshape.reshape(labels[i]));
        ivKamancheh.setImageResource(imgs[i]);
        Button[] btns = {Kamancheh1, Kamancheh2, Kamancheh3, Kamancheh4};
        selectStringButton(btns[i]);
    }

    private void selectKook(double[] tones, RadioButton checked) {
        Miditone = tones;
        miLaReSolRB.setChecked(checked == miLaReSolRB);
        reLaReLaRB.setChecked(checked == reLaReLaRB);
        miLaMiLaRB.setChecked(checked == miLaMiLaRB);
        reLaReSolRB.setChecked(checked == reLaReSolRB);
        kookSelected = true;
        selectedStringIndex = -1;
        lastTunedIndex = -1;
        Kamancheh1.setEnabled(true);
        Kamancheh2.setEnabled(true);
        Kamancheh3.setEnabled(true);
        Kamancheh4.setEnabled(true);
        resetStringButtonTints();
        ivKamancheh.setImageResource(R.drawable.kamancheh);
        pitchView.setCenterPitch(0);
        pitchLabel.setText(PersianReshape.reshape("سیم مورد نظر جهت کوک کردن را انتخاب کنید"));
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tuning) {
            tuning.setChecked(!tuning.isChecked());
        } else if (viewId == R.id.kamancheh_1) {
            if (kookSelected) selectString(0, R.drawable.kamancheh1, "\u0633\u06CC\u0645 \u0627\u0648\u0644");
        } else if (viewId == R.id.kamancheh_2) {
            if (kookSelected) selectString(1, R.drawable.kamancheh2, "\u0633\u06CC\u0645 \u062F\u0648\u0645");
        } else if (viewId == R.id.kamancheh_3) {
            if (kookSelected) selectString(2, R.drawable.kamancheh3, "\u0633\u06CC\u0645 \u0633\u0648\u0645");
        } else if (viewId == R.id.kamancheh_4) {
            if (kookSelected) selectString(3, R.drawable.kamancheh4, "\u0633\u06CC\u0645 \u0686\u0647\u0627\u0631\u0645");
        } else if (viewId == R.id.MilaReSolRB) {
            selectKook(new double[]{74, 67, 60, 53}, miLaReSolRB);
        } else if (viewId == R.id.ReLaRelaRB) {
            selectKook(new double[]{72, 67, 60, 55}, reLaReLaRB);
        } else if (viewId == R.id.MiLaMilaRB) {
            selectKook(new double[]{74, 67, 62, 55}, miLaMiLaRB);
        } else if (viewId == R.id.ReLaReSolRB) {
            selectKook(new double[]{72, 67, 60, 53}, reLaReSolRB);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinnerKookChange) {
            switch (position) {
                case 0: nimpardehSub = 0; break;
                case 1: nimpardehSub = 0.5; break;
                case 2: nimpardehSub = 1; break;
                case 3: nimpardehSub = 2; break;
            }
            if (selectedStringIndex >= 0) {
                triggerNote2((float) (Miditone[selectedStringIndex] - nimpardehSub));
            }
            if (getprefs != null) {
                SharedPreferences.Editor editor = getprefs.edit();
                editor.putInt("change", position);
                editor.apply();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
