package nwaah.trk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Random;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences preferences;
    SeekBar freq_bar_minutes;
    SeekBar freq_bar_seconds;
    SeekBar min_step_bar;
    SeekBar max_step_bar;
    EditText freq_val_minutes;
    EditText freq_val_seconds;
    EditText min_step_val;
    EditText max_step_val;
    CheckBox draw_circles;
    RadioButton providerg;
    RadioButton providern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        setContentView(R.layout.activity_settings);

        freq_bar_minutes = (SeekBar) findViewById(R.id.settings_frequency_bar_minutes);
        freq_bar_seconds = (SeekBar) findViewById(R.id.settings_frequency_bar_seconds);
        min_step_bar = (SeekBar) findViewById(R.id.settings_min_step_bar);
        max_step_bar = (SeekBar) findViewById(R.id.settings_max_step_bar);
        freq_val_minutes = (EditText) findViewById(R.id.settings_frequency_value_minutes);
        freq_val_seconds = (EditText) findViewById(R.id.settings_frequency_value_seconds);
        min_step_val = (EditText) findViewById(R.id.settings_min_step_value);
        max_step_val = (EditText) findViewById(R.id.settings_max_step_value);
        draw_circles =  (CheckBox) findViewById(R.id.settings_draw_circles);
    }

    @Override
    protected void onResume() {
        super.onResume();

        freq_val_minutes.setText(String.valueOf(preferences.getInt(Const.preferences_key_setting_frequency, 0)/60));
        freq_val_seconds.setText(String.valueOf(preferences.getInt(Const.preferences_key_setting_frequency, 30)%61));
        min_step_val.setText(String.valueOf(preferences.getInt(Const.preferences_key_setting_min_step, 0)));
        max_step_val.setText(String.valueOf(preferences.getInt(Const.preferences_key_setting_max_step, 0)));
        freq_bar_minutes.setProgress(preferences.getInt(Const.preferences_key_setting_frequency, 30)/60);
        freq_bar_seconds.setProgress(preferences.getInt(Const.preferences_key_setting_frequency, 30)%61);
        min_step_bar.setProgress(preferences.getInt(Const.preferences_key_setting_min_step, 0));
        max_step_bar.setProgress(preferences.getInt(Const.preferences_key_setting_max_step, 0));
        draw_circles.setChecked(preferences.getBoolean(Const.preferences_key_setting_draw_circles, false));
        providerg = (RadioButton) findViewById(R.id.provider_GPS);
        providern = (RadioButton) findViewById(R.id.provider_Network);
        if(preferences.getString(Const.preferences_key_setting_provider, LocationManager.GPS_PROVIDER).equals(LocationManager.GPS_PROVIDER))
            providerg.setChecked(true);
        else
            providern.setChecked(false);

            setOnChangedListener(freq_bar_minutes, freq_val_minutes);
        setOnChangedListener(freq_bar_seconds, freq_val_seconds);
        setOnChangedListener(min_step_bar, min_step_val);
        setOnChangedListener(max_step_bar, max_step_val);
    }

    private void setOnChangedListener(final SeekBar bar, final EditText val) {
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                val.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        val.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                int value = Integer.parseInt(val.getText().toString());
                if (value < 0)
                    value = 0;
                if (value > bar.getMax())
                    bar.setProgress(bar.getMax());
                else
                    bar.setProgress(value);
            }
        });
    }

    public void saveSettings(View view) {
        int setting_freq_minutes = Integer.parseInt(freq_val_minutes.getText().toString());
        int setting_freq_seconds = Integer.parseInt(freq_val_seconds.getText().toString());
        int setting_min_step = Integer.parseInt(min_step_val.getText().toString());
        int setting_max_step = Integer.parseInt(max_step_val.getText().toString());

        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(Const.preferences_key_setting_frequency, setting_freq_minutes * 60 + setting_freq_seconds);
        editor.putInt(Const.preferences_key_setting_min_step, setting_min_step);
        editor.putInt(Const.preferences_key_setting_max_step, setting_max_step);

        editor.putBoolean(Const.preferences_key_setting_draw_circles, draw_circles.isChecked());
        editor.putString(Const.preferences_key_setting_provider, providerg.isChecked()?LocationManager.GPS_PROVIDER:LocationManager.NETWORK_PROVIDER);
        editor.apply();
        preferences = null;

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(new Intent(Const.intent_filter_action_settings_updated));

        onBackPressed();
    }

    public void clear(View view) {
        DatabaseHelper db = new DatabaseHelper(this);
        db.clear();
        db.reset();
        db.close();
    }

    public void addRandomPoint(View view) {
        int trackid = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).getInt(Const.current_track_id, 0);
        Random rand = new Random();
        Point pt = new Point(
                rand.nextDouble() * 180 - 90,
                rand.nextDouble() * 180 - 90,
                rand.nextDouble() * 100,
                String.valueOf(rand.nextInt(1400)),
                trackid
        );
        new DatabaseHelper(this).savePoint(pt);
        Log.d("Settings", "Added random point " + pt.toString());
    }
}
