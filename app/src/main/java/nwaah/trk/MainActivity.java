package nwaah.trk;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    public DatabaseHelper db;

    int currentTrackId;
    String currentTrackName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                MODE_PRIVATE);
        currentTrackId = preferences.getInt("CurrentTrack", 0);
        currentTrackName = preferences.getString("CurrentTrackName", "");


        setContentView(R.layout.activity_main);
        TextView name = (TextView) findViewById(R.id.currentTrackName);
        name.setText(currentTrackName);

        db = new DatabaseHelper(this);

        displayTrackDetails(currentTrackId);
    }

    private void displayTrackDetails(int currentTrackId) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                MODE_PRIVATE);
        currentTrackId = preferences.getInt("CurrentTrack", 0);
        currentTrackName = preferences.getString("CurrentTrackName", "");

        TextView name = (TextView)findViewById(R.id.currentTrackName);
        name.setText(currentTrackName);
    }

    public void showSettings(View view) {
        Intent intent = new Intent();
        intent.setClass(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void message(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void testWrite(View view) {

        Point p = new Point(10, 100, 1);
        long id = db.savePoint(p);
        db.saveTrack("Nowa Trasa1");
        TextView tv = (TextView) findViewById(R.id.text);
        message(p.toString()+" saved");
        tv.setText(p.toString());
        tv.setText(String.valueOf(id));
    }


    public void testRead2(View view) {
        TextView tv = (TextView) findViewById(R.id.text);
        String txt="";
        Point[] pts = db.getPoints(1);
        for(int i=0; i<pts.length; i++)
            txt+=pts[i].toString()+"\n";
        tv.setText(txt);
    }

    public void DBcheck(View view) {
        SQLiteDatabase a = db.getReadableDatabase();

    }

    public void clear(View view) {
        db.clear();
        db.reset();
    }

    public void chooseTrack(View view) {
        //Intent intent = new Intent(this, TrackChooseActivity.class);
        Intent intent = new Intent(this, TrackChoose.class);
        startActivity(intent);
    }

    public void setService(View view) {
        ToggleButton button = (ToggleButton)findViewById(R.id.toggleButton);
        Intent intent = new Intent(this, TrackingService.class);
        if(button.isChecked())
            startService(intent);
        else
            stopService(intent);
    }

    public boolean setCurrentTrack(int trackId)
    {
        this.currentTrackId = trackId;
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void showMap(View view) {
        startActivity(new Intent(this, MapsActivity.class));
    }
}
