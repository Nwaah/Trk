package nwaah.trk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.larswerkman.holocolorpicker.ColorPicker;

public class AddTrack extends AppCompatActivity {

    ColorPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);

        picker = (ColorPicker) findViewById(R.id.picker);
        picker.setShowOldCenterColor(false);
    }

    public void cancel(View view) {
        this.onBackPressed();
    }

    public void save(View view) {
        int color = picker.getColor();
        EditText text = (EditText) findViewById(R.id.add_track_name);
        String name = text.getText().toString();
        if (!name.isEmpty()) {
            long id = new DatabaseHelper(this).saveTrack(name, color);
            int arg_id = (int) id;
            setCurrentTrack(arg_id, name);
            onBackPressed();
        }
    }

    public void setCurrentTrack(int id, String name) {

        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.app_name),
                MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();
        ed.remove(Const.current_track_id);
        ed.apply();
        ed.putInt(Const.current_track_id, id);
        ed.apply();
        ed.remove(Const.current_track_name);
        ed.apply();
        ed.putString(Const.current_track_name, name);
        ed.apply();
        stopService(new Intent(this, TrackingService.class));
    }
}
