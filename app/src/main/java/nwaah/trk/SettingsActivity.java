package nwaah.trk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;

import java.util.Random;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences preferences;
    SeekBar freq_bar_minutes;
    SeekBar freq_bar_seconds;
    SeekBar step_bar;
    EditText freq_val_minutes;
    EditText freq_val_seconds;
    EditText step_val;
    CheckBox draw_circles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        setContentView(R.layout.activity_settings);

        freq_bar_minutes = (SeekBar) findViewById(R.id.settings_frequency_bar_minutes);
        freq_bar_seconds = (SeekBar) findViewById(R.id.settings_frequency_bar_seconds);
        step_bar = (SeekBar) findViewById(R.id.settings_step_bar);
        freq_val_minutes = (EditText) findViewById(R.id.settings_frequency_value_minutes);
        freq_val_seconds = (EditText) findViewById(R.id.settings_frequency_value_seconds);
        step_val = (EditText) findViewById(R.id.settings_step_value);
        draw_circles =  (CheckBox) findViewById(R.id.settings_draw_circles);
    }

    @Override
    protected void onResume() {
        super.onResume();

        freq_val_minutes.setText(String.valueOf(preferences.getInt(Const.preferences_key_setting_frequency, 0)/60));
        freq_val_seconds.setText(String.valueOf(preferences.getInt(Const.preferences_key_setting_frequency, 30)%61));
        step_val.setText(String.valueOf(preferences.getInt(Const.preferences_key_setting_step, 0)));
        freq_bar_minutes.setProgress(preferences.getInt(Const.preferences_key_setting_frequency, 30)/60);
        freq_bar_seconds.setProgress(preferences.getInt(Const.preferences_key_setting_frequency, 30)%61);
        step_bar.setProgress(preferences.getInt(Const.preferences_key_setting_step, 0));
        draw_circles.setChecked(preferences.getBoolean(Const.preferences_key_setting_draw_circles, false));

        setOnChangedListener(freq_bar_minutes, freq_val_minutes);
        setOnChangedListener(freq_bar_seconds, freq_val_seconds);
        setOnChangedListener(step_bar, step_val);
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
        int setting_step = Integer.parseInt(step_val.getText().toString());

        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(Const.preferences_key_setting_frequency, setting_freq_minutes * 60 + setting_freq_seconds);
        editor.putInt(Const.preferences_key_setting_step, setting_step);

        editor.putBoolean(Const.preferences_key_setting_draw_circles, draw_circles.isChecked());

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
