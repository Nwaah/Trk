package nwaah.trk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.larswerkman.holocolorpicker.ColorPicker;

public class EditTrack extends AppCompatActivity {

    int id;
    ColorPicker picker;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this);
        setContentView(R.layout.activity_rename_track);
        EditText text = (EditText) findViewById(R.id.track_rename_name);
        id = getIntent().getIntExtra(DatabaseHelper.KEY_ID, 0);

        int lastColor = db.getTrackColor(id);
        picker = (ColorPicker) findViewById(R.id.picker);
        picker.setOldCenterColor(lastColor);
        picker.setNewCenterColor(lastColor);
        picker.setShowOldCenterColor(true);

        String name = db.getTrackName(id);
        text.setText(db.getTrackName(id));
    }

    public void cancel(View view) {
        this.onBackPressed();
    }

    public void rename(View view) {
        EditText text = (EditText) findViewById(R.id.track_rename_name);
        String name = text.getText().toString();
        int color = picker.getColor();
        if (!name.isEmpty()) {
            new DatabaseHelper(this).editTrack(id, name, color);
            Log.d("EditTrack", "Track edited");
        }
        onBackPressed();
    }
}
