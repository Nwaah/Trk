package nwaah.trk;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper db;

    int currentTrackId;
    BroadcastReceiver refreshViewsReceiver;
    BroadcastReceiver changeButtonReceiver;
    LocalBroadcastManager broadcastManager;

    public static void requestLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Activity instance = new MainActivity();
            instance.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        refreshViewsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Const.intent_filter_action_refresh_views)
                    setDetails(currentTrackId);
            }
        };
        changeButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Const.intent_filter_action_order_button_change)
                    setServiceButton();
            }
        };


        broadcastManager = LocalBroadcastManager.getInstance(this);

        Log.d("Main", "OnCreate completed");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        currentTrackId = preferences.getInt(Const.current_track_id, 0);
        String currentTrackName = db.getTrackName(currentTrackId);
        Log.d("Main", "Track data taken from prefs");

        setCurrentTrackName(currentTrackName);
        setDetails(currentTrackId);

        Log.d("Main", "Track name displayed");
        Log.d("Main", "Track details displayed");

        broadcastManager.registerReceiver(refreshViewsReceiver, new IntentFilter(Const.intent_filter_action_refresh_views));
        broadcastManager.registerReceiver(changeButtonReceiver, new IntentFilter(Const.intent_filter_action_order_button_change));
        Log.d("Main", "OnResume completed");

        resetServiceButton();
    }

    @Override
    protected void onPause() {
        broadcastManager.unregisterReceiver(refreshViewsReceiver);
        broadcastManager.unregisterReceiver(changeButtonReceiver);
        Log.d("Main", "Receivers unregistered");
        super.onPause();
    }

    private void setDetails(int currentTrackId) {
        TextView details = (TextView) findViewById(R.id.currentTrackDetails);
        details.setText(db.getTrackDetails(currentTrackId));
    }

    private void setCurrentTrackName(String currentTrackName) {
        TextView name = (TextView) findViewById(R.id.currentTrackName);
        name.setText(currentTrackName);
    }

    private void resetServiceButton() {
        ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);
        button.setChecked(false);
        pingService();
    }

    private void pingService() {
        Intent ping = new Intent(Const.intent_filter_action_ping_service);
        broadcastManager.sendBroadcast(ping);
        Log.d("Main", "Ping to service sent");
    }

    public void setServiceButton() {
        ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);
        if (!button.isChecked())
            button.setChecked(true);
        Log.d("Main", "Button set from service order");
    }

    public void showSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void message(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void testRead2(View view) {
        TextView tv = (TextView) findViewById(R.id.text);
        String txt = "";
        for (Point point : db.getPoints(currentTrackId)
                ) {
            txt += point.toString() + "\n";
        }
        tv.setText(txt);
    }

    public void DBcheck(View view) {
        SQLiteDatabase a = db.getReadableDatabase();

    }

    public void chooseTrack(View view) {
        Intent intent = new Intent(this, TrackChoose.class);
        startActivity(intent);
    }

    public void setService(View view) {
        ToggleButton button = (ToggleButton) findViewById(R.id.toggleButton);
        Intent intent = new Intent(this, TrackingService.class);
        if (button.isChecked())
            startService(intent);
        else
            stopService(intent);
    }

    public boolean setCurrentTrack(int trackId) {
        this.currentTrackId = trackId;
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("Main", "Permission request result: " + grantResults.toString());
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void showMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(Const.key_id, currentTrackId);
        startActivity(intent);
    }
}
