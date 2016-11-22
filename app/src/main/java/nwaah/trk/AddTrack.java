package nwaah.trk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AddTrack extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);
    }

    public void cancel(View view) {
        this.onBackPressed();
    }

    public void save(View view) {
        EditText text = (EditText) findViewById(R.id.add_track_name);
        String name = text.getText().toString();
        long id = new DatabaseHelper(this).saveTrack(name);
        int arg_id = (int) id;
        setCurrentTrack(arg_id, name);
        onBackPressed();
    }

    public void setCurrentTrack(int id, String name) {

        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.app_name),
                MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();
        ed.remove("CurrentTrack");
        ed.apply();
        ed.putInt("CurrentTrack", id);
        ed.apply();
        ed.remove("CurrentTrackName");
        ed.apply();
        ed.putString("CurrentTrackName", name);
        ed.apply();
        stopService(new Intent(this, TrackingService.class));
    }
}
