package com.PouyaApp.kookyarkamancheh;

import com.kookyar.common.EdgeToEdge;
import com.kookyar.common.TextPageActivity;
import com.kookyar.common.KookyarFonts;
import com.kookyar.common.Anim;
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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.materialswitch.MaterialSwitch;

import com.kookyar.common.FeatureBar;
import com.kookyar.common.MetronomeLauncher;
import com.kookyar.common.RadifLauncher;
import com.kookyar.common.PersianReshape;
import com.kookyar.common.PitchView;
import com.kookyar.common.InstrumentView;
import com.kookyar.common.TunerPitchCoordinator;


public class KamanchehTuner extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "KookYaR";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1001;

    final private int highSensitive = 55;
    final private int midSensitive = 50;
    final private int lowSensitive = 45;

    private PdUiDispatcher dispatcher;
    public String fonts = "BZar.ttf";
    private InstrumentView ivKamancheh;
    private Button Kamancheh1, Kamancheh2, Kamancheh3, Kamancheh4;
    private TextView pitchLabel;
    private PitchView pitchView;
    private MaterialSwitch tuning;
    private Spinner kookSpinner;
    private Spinner kookChangeSpinner;
    private boolean kookSelected = false;
    OnSharedPreferenceChangeListener listener;
    SharedPreferences getprefs;
    private int laFrequens = 440;
    private int sensitiveLevel = midSensitive;
    private int pressure;
    private double nimpardehSub = 0;
    private int selectedStringIndex = -1;
    private int lastTunedIndex = -1;
    private final TunerPitchCoordinator pitchCoordinator = new TunerPitchCoordinator();

    private double[] Miditone = new double[4];

    private PdService pdService = null;

    /** Flat switcher: every feature is one tap from the tuner. */
    private void initFeatureBar() {
        FeatureBar featureBar = findViewById(R.id.feature_bar);
        featureBar.setAccentColor(ContextCompat.getColor(this, R.color.accentColor));
        featureBar.setInactiveColor(ContextCompat.getColor(this, R.color.onSurfaceVariant));
        featureBar.setItems(
                FeatureBar.Item.current(
                        getString(com.kookyar.common.R.string.kookyar_feature_tuner),
                        com.kookyar.common.R.drawable.ic_kookyar_tuner),
                FeatureBar.Item.of(
                        getString(com.kookyar.common.R.string.kookyar_feature_metronome),
                        com.kookyar.common.R.drawable.ic_kookyar_metronome,
                        () -> MetronomeLauncher.open(this)),
                FeatureBar.Item.of(
                        getString(com.kookyar.common.R.string.kookyar_feature_radif),
                        com.kookyar.common.R.drawable.ic_kookyar_radif,
                        () -> RadifLauncher.open(this)),
                FeatureBar.Item.of(
                        getString(com.kookyar.common.R.string.kookyar_feature_help),
                        com.kookyar.common.R.drawable.ic_kookyar_help,
                        () -> TextPageActivity.open(this, R.string.rahnama, R.string.TunerHelp)),
                FeatureBar.Item.of(
                        getString(com.kookyar.common.R.string.kookyar_feature_settings),
                        com.kookyar.common.R.drawable.ic_kookyar_settings,
                        () -> startActivity(new Intent(this, Prefs.class))));
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
		EdgeToEdge.apply(this, findViewById(R.id.tuner_root), findViewById(R.id.app_bar), findViewById(R.id.feature_bar));
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
                Toast.makeText(this, com.kookyar.common.R.string.kookyar_mic_required, Toast.LENGTH_LONG).show();
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


    /** Stop the microphone and audio thread while the screen is not visible. */
    @Override
    protected void onStart() {
        super.onStart();
        if (pdService != null && !pdService.isRunning()) {
            pdService.startAudio();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pdService != null && pdService.isRunning()) {
            pdService.stopAudio();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pitchCoordinator.destroy();
        try {
            unbindService(pdConnection);
        } catch (Exception e) {
            // ignore if not bound
        }
    }

    private void initGui() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        Typeface face = KookyarFonts.get(this, fonts);

        ivKamancheh = findViewById(R.id.layout_sim);
        ivKamancheh.setOnStringSelectedListener(this::selectString);
        Kamancheh1 = (Button) findViewById(R.id.kamancheh_1);
        Kamancheh1.setOnClickListener(this);
        Kamancheh1.setTypeface(face);
        Kamancheh1.setText(PersianReshape.forDisplay(this, Kamancheh1.getText().toString()));

        Kamancheh2 = (Button) findViewById(R.id.kamancheh_2);
        Kamancheh2.setOnClickListener(this);
        Kamancheh2.setTypeface(face);
        Kamancheh2.setText(PersianReshape.forDisplay(this, Kamancheh2.getText().toString()));

        Kamancheh3 = (Button) findViewById(R.id.kamancheh_3);
        Kamancheh3.setOnClickListener(this);
        Kamancheh3.setTypeface(face);
        Kamancheh3.setText(PersianReshape.forDisplay(this, Kamancheh3.getText().toString()));

        Kamancheh4 = (Button) findViewById(R.id.kamancheh_4);
        Kamancheh4.setOnClickListener(this);
        Kamancheh4.setTypeface(face);
        Kamancheh4.setText(PersianReshape.forDisplay(this, Kamancheh4.getText().toString()));

        pitchLabel = (TextView) findViewById(R.id.pitch_label);
        pitchView = (PitchView) findViewById(R.id.pitch_view);

        getprefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        kookSpinner = (Spinner) findViewById(R.id.kookSpiner);
        ArrayAdapter<CharSequence> kookAdapter = ArrayAdapter.createFromResource(this, R.array.kouk, R.layout.spinner_item);
        kookAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        kookSpinner.setAdapter(kookAdapter);
        kookSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyKookPreset(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
        kookChangeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyTranspose(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        int savedKook = getprefs.getInt("kook", 0);
        if (savedKook < 0 || savedKook >= kookAdapter.getCount()) {
            savedKook = 0;
        }
        kookSpinner.setSelection(savedKook, false);
        applyKookPreset(savedKook);

        int savedTranspose = getprefs.getInt("change", 3);
        if (savedTranspose < 0 || savedTranspose > 3) {
            savedTranspose = 3;
        }
        kookChangeSpinner.setSelection(savedTranspose, false);
        applyTranspose(savedTranspose);

        tuning = (MaterialSwitch) findViewById(R.id.checkBox_tuning);
        findViewById(R.id.tuning).setOnClickListener(this);
        tuning.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                lastTunedIndex = -1;
                if (isChecked) {
                    Toast.makeText(getApplicationContext(),
                            com.kookyar.common.R.string.kookyar_auto_detect_hint,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        pitchLabel.setText(getString(com.kookyar.common.R.string.kookyar_gauge_pick_string));

        resetStringButtonTints();

        initFeatureBar();
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

    private void applyKookPreset(int position) {
        switch (position) {
            case 0:  Miditone = new double[]{76, 69, 62, 55}; break;   // می-لا-ر-سل
            case 1:  Miditone = new double[]{76, 69, 62, 52}; break;   // شور: می-لا-ر-می
            case 2:  Miditone = new double[]{76, 69, 64, 57}; break;   // ابوعطا: می-لا-می-لا
            case 3:  Miditone = new double[]{76, 69, 62, 55}; break;   // بیات ترک: می-لا-ر-سل
            case 4:  Miditone = new double[]{76, 69, 62, 57}; break;   // افشاری و ماهور: می-لا-ر-لا
            case 5:  Miditone = new double[]{76, 69, 59, 55}; break;   // دشتی: می-لا-سی-سل
            case 6:  Miditone = new double[]{76, 69, 62, 53.5}; break; // همایون و سه‌گاه: می-لا-ر-فا سری
            case 7:  Miditone = new double[]{76, 69, 64, 57}; break;   // اصفهان: می-لا-می-لا
            case 8:  Miditone = new double[]{76, 69, 64, 52}; break;   // شوشتری: می-لا-می-می
            case 9:  Miditone = new double[]{76, 69, 62, 57}; break;   // چهارگاه: می-لا-ر-لا
            case 10: Miditone = new double[]{74, 69, 62, 57}; break;   // ر-لا-ر-لا
            case 11: Miditone = new double[]{76, 69, 64, 53.5}; break; // می-لا-می-فا سری
            default: Miditone = new double[]{74, 67, 60, 53}; break;
        }

        kookSelected = true;
        selectedStringIndex = -1;
        lastTunedIndex = -1;
        resetStringButtonTints();
        ivKamancheh.setSelectedString(-1);
        ivKamancheh.setStringNotes(displayedNotes());
        pitchView.setCenterPitch(0);
        pitchLabel.setText(getString(com.kookyar.common.R.string.kookyar_gauge_pick_string));

        if (getprefs != null) {
            getprefs.edit().putInt("kook", position).apply();
        }
    }

    private void applyTranspose(int position) {
        switch (position) {
            case 0: nimpardehSub = 0; break;
            case 1: nimpardehSub = 0.5; break;
            case 2: nimpardehSub = 1; break;
            case 3: nimpardehSub = 2; break;
            default: nimpardehSub = 0; break;
        }
        ivKamancheh.setStringNotes(displayedNotes());

        if (selectedStringIndex >= 0) {
            triggerNote2((float) (Miditone[selectedStringIndex] - nimpardehSub));
        }

        if (getprefs != null) {
            getprefs.edit().putInt("change", position).apply();
        }
    }

    private void initPd() throws IOException {
        AudioParameters.init(this);
        int sampleRate = AudioParameters.suggestSampleRate();
        pdService.initAudio(sampleRate, 1, 2, 20.0f);
        start();
        pitchCoordinator.setAudioStarter(() -> {
            if (pdService != null && !pdService.isRunning()) {
                pdService.startAudio();
            }
        });

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);
        dispatcher.addListener("pitch", new PdListener.Adapter() {
            @Override
            public void receiveFloat(String source, final float x) {
                if (pressure >= sensitiveLevel && pitchView.getCenterPitch() > 0) {
                    final float pitch = (float) Math.round(x * 1000) / 1000;
                    pitchCoordinator.onPitchInput(
                            () -> {
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
                            },
                            () -> pitchView.setCurrentPitch(12));
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
        pitchCoordinator.invalidatePitchReset();
        pitchView.setCenterPitch(realMidi);
        pitchView.setCurrentPitch(12);
        pitchView.setMidiRef(n);
        final float midiForPd = realMidi;
        pitchCoordinator.scheduleTrigger(() -> TunerPitchCoordinator.runPdSend(() -> {
            PdBase.sendFloat("midinote", midiForPd);
            PdBase.sendBang("trigger");
        }));
    }

    private void triggerNote2(float n) {
        pitchCoordinator.invalidatePitchReset();
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

    private void selectString(int i) {
        selectedStringIndex = i;
        lastTunedIndex = -1;
        triggerNote((float) (Miditone[i] - nimpardehSub));
        pitchLabel.setText(PersianReshape.forDisplay(this, stringLabel(i)));
        ivKamancheh.setSelectedString(i);
        Anim.landing(ivKamancheh);
        Button[] btns = {Kamancheh1, Kamancheh2, Kamancheh3, Kamancheh4};
        selectStringButton(btns[i]);
    }

    /** "String n" in the current language. */
    private String stringLabel(int i) {
        int[] names = {R.string.sim_1, R.string.sim_2, R.string.sim_3, R.string.sim_4};
        return getString(names[i]);
    }

    /** Notes the picture labels its strings with: the tuning less the transpose. */
    private double[] displayedNotes() {
        double[] notes = new double[Miditone.length];
        for (int i = 0; i < notes.length; i++) {
            notes[i] = Miditone[i] - nimpardehSub;
        }
        return notes;
    }

    private void autoTune(int i) {
        if (i == lastTunedIndex) return;
        lastTunedIndex = i;
        selectedStringIndex = i;
        triggerNote2((float) (Miditone[i] - nimpardehSub));
        pitchLabel.setText(PersianReshape.forDisplay(this, stringLabel(i)));
        ivKamancheh.setSelectedString(i);
        Button[] btns = {Kamancheh1, Kamancheh2, Kamancheh3, Kamancheh4};
        selectStringButton(btns[i]);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.tuning) {
            tuning.setChecked(!tuning.isChecked());
        } else if (viewId == R.id.kamancheh_1) {
            selectString(0);
        } else if (viewId == R.id.kamancheh_2) {
            selectString(1);
        } else if (viewId == R.id.kamancheh_3) {
            selectString(2);
        } else if (viewId == R.id.kamancheh_4) {
            selectString(3);
        }
    }

}
