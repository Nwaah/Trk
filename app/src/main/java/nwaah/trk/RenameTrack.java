package nwaah.trk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RenameTrack extends AppCompatActivity {

    int id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rename_track);
        EditText text = (EditText)findViewById(R.id.track_rename_name);
        id = getIntent().getIntExtra(DatabaseHelper.KEY_ID,0);

        String name = getIntent().getStringExtra(DatabaseHelper.KEY_NAME);
        text.setText(name);
    }

    public void cancel(View view) {
        this.onBackPressed();
    }

    public void rename(View view) {
        EditText text = (EditText) findViewById(R.id.track_rename_name);
        String name = text.getText().toString();
        if(!name.isEmpty()){
            new DatabaseHelper(this).renameTrack(id, name);
            Toast.makeText(this, "Nazwa trasy zmieniona na "+name, Toast.LENGTH_SHORT).show();
        }
        onBackPressed();
    }
}
