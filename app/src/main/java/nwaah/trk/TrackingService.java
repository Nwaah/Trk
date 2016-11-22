package nwaah.trk;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.security.Permission;
import java.security.Provider;
import java.util.Timer;
import java.util.TimerTask;

public class TrackingService extends Service {
    public TrackingService() {
    }

    private DatabaseHelper db;
    private LocationManager locationManager;
    private LocationListener locationListener;

    int frequency;
    int currentTrackId;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DatabaseHelper(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(new MainActivity(), new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION}, R.string.app_name);
            Toast.makeText(this, "Usługa nie ma pozwolenia na użycie sygnału GPS", Toast
                    .LENGTH_LONG).show();
            stopService(new Intent(this, this.getClass()));
        }
        else {
            SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                    MODE_PRIVATE);
            frequency = preferences.getInt("Frequency", 30000);
            currentTrackId = preferences.getInt("CurrentTrack", 0);
            preferences = null;
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (savePoint(location))
                        Toast.makeText(getApplicationContext(), "Point saved", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Point not saved", Toast.LENGTH_SHORT).show();
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

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, frequency, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, frequency, 0,
                    locationListener);
            Toast.makeText(this, "Śledzenie włączone", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean savePoint(Location location) {
        try{
            Point point = new Point(
                    location.getLatitude(),
                    location.getLongitude(),
                    String.valueOf(location.getTime()),
                    currentTrackId);
            db.savePoint(point);
            return true;
        }
        catch (Exception e){
            return false;
        }

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Śledzenie wyłączone", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

}
