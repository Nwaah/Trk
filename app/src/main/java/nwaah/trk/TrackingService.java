package nwaah.trk;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class TrackingService extends Service {
    int frequency;
    int minStep;
    int currentTrackId;
    private DatabaseHelper db;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private BroadcastReceiver pingReceiver;
    private BroadcastReceiver settingsChangedReceiver;
    private LocalBroadcastManager broadcastManager;
    private String provider;
    private boolean show_notifications;
    public TrackingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DatabaseHelper(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        pingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Const.intent_filter_action_ping_service)) {
                    orderButtonChange();
                }
            }
        };

        settingsChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Const.intent_filter_action_settings_updated)) {
                    updateSettings();
                    setLocationListener();
                }
            }
        };

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                long id = savePoint(location);
                if (id > -1) {
                    if(show_notifications)
                        Toast.makeText(getApplicationContext(), "Dodano punkt", Toast.LENGTH_SHORT).show();

                    Intent pointAdded = new Intent(Const.intent_filter_action_refresh_views);
                    pointAdded.putExtra(Const.key_id, id);
                    Log.d("Service", "New point added");

                    broadcastManager.sendBroadcast(pointAdded);
                } else {
                    Log.d("Service", "Error during adding new point");
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        Log.d("Service", "Service created " + frequency + "s - " + minStep + "m");
    }

    private void setLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(new MainActivity(), new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION}, R.string.app_name);
            MainActivity.requestLocationPermissions();
            stopService(new Intent(this, this.getClass()));
        } else {
            try
            {locationManager.removeUpdates(locationListener);} catch(Exception e){e.printStackTrace();}
            locationManager.requestLocationUpdates(provider, frequency * 1000, minStep, locationListener);
            Log.d("Service", "Location updates requested");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateSettings();
        setLocationListener();

        broadcastManager.registerReceiver(settingsChangedReceiver, new IntentFilter(Const.intent_filter_action_settings_updated));
        broadcastManager.registerReceiver(pingReceiver, new IntentFilter(Intent.ACTION_ANSWER));

        Toast.makeText(this, "Śledzenie włączone", Toast.LENGTH_SHORT).show();

        Log.d("Service", "Service started");

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        broadcastManager.unregisterReceiver(pingReceiver);
        broadcastManager.unregisterReceiver(settingsChangedReceiver);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            MainActivity.requestLocationPermissions();
        }
        locationManager.removeUpdates(locationListener);
        Toast.makeText(this, "Śledzenie wyłączone", Toast.LENGTH_SHORT).show();
        Log.d("Service", "Location updates stopped");

        Log.d("Service", "Service killed");

        super.onDestroy();
    }

    private void orderButtonChange() {
        Intent intent = new Intent(Const.intent_filter_action_order_button_change);
        broadcastManager.sendBroadcast(intent);
        Log.d("Service", "Ping received, sending button change order");
    }

    private long savePoint(Location location) {
        try {
            Point point = new Point(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    String.valueOf(location.getTime()),
                    currentTrackId);

            return db.savePoint(point);
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateSettings() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                MODE_PRIVATE);
        frequency = preferences.getInt(Const.preferences_key_setting_frequency, 30);
        currentTrackId = preferences.getInt(Const.current_track_id, 0);
        minStep = preferences.getInt(Const.preferences_key_setting_min_step, 0);
        provider = preferences.getString(Const.preferences_key_setting_provider, LocationManager.GPS_PROVIDER);
        show_notifications = preferences.getBoolean(Const.preferences_key_setting_show_notifications, true);

        Log.d("Service","Settings taken from preferences");
    }
}
