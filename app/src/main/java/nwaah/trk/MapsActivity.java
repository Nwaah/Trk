package nwaah.trk;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    DatabaseHelper db;
    ArrayList<colorTrack> tracks = new ArrayList<>();
    int currentTrackId;
    LocalBroadcastManager broadcastManager;
    BroadcastReceiver pointAddedReceiver;
    colorTrack currentTrack = null;
    MapsActivity self;
    private GoogleMap map;
    Marker goal_marker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = new DatabaseHelper(this);
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        currentTrackId = preferences.getInt(Const.current_track_id, 0);

        broadcastManager = LocalBroadcastManager.getInstance(this);
        pointAddedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Const.intent_filter_action_refresh_views) && currentTrack != null) {
                    currentTrack.addPoint(db.getPoint(intent.getIntExtra(Const.key_id, 0)));
                    drawTrack(currentTrack);
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        int requestedTrack = getIntent().getIntExtra(Const.key_id, 0);
        if (requestedTrack > 0) {
            addTrack(requestedTrack);
        }
        Log.d("Map", "onStart: requested track: " + requestedTrack + "added");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/


        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            map.setMyLocationEnabled(true);
            setOnLongClickListener();
            setOnTrackClickListener();
            setMarkerFromPreferences();
            broadcastManager.registerReceiver(pointAddedReceiver, new IntentFilter(Const.intent_filter_action_refresh_views));

            drawAllAndCenter();
        }

    }

    @Override
    protected void onPause() {
        broadcastManager.unregisterReceiver(pointAddedReceiver);
        super.onPause();
    }

    private void drawAll() {
        for (colorTrack track : tracks) {
            drawTrack(track);
        }
    }

    private void addTrack(int trackId) {
        colorTrack ayylmao = new colorTrack();
        ayylmao.id = trackId;
        ayylmao.track = db.getPoints(trackId);
        ayylmao.color = db.getTrackColor(trackId);
        tracks.add(ayylmao);
        if(ayylmao.id == currentTrackId)
            currentTrack = ayylmao;
        Log.d("Map", "Track " + ayylmao.id + " added");
    }

    private void removeTrack(int trackid) {
        for (colorTrack track : tracks) {
            if (track.id == trackid)
            {
                tracks.remove(track);
                if(trackid == currentTrackId)
                    currentTrack = null;
                break;
            }
        }
    }

    private void centerOnEnd(colorTrack track) {
        centerOnEnd(track.track);
    }

    private void centerOnEnd(ArrayList<Point> points) {
        if (points.size() < 1)
            return;

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                new LatLng(
                        points.get(points.size() - 1).getLatitude(),
                        points.get(points.size() - 1).getLongitude()
                ), map.getMinZoomLevel() + 2
        );

        map.animateCamera(update);
        Log.d("Map", "View centered on end of track");
    }

    private void drawAllAndCenter() {
        drawAll();
        centerOnEnd(tracks.get(0));
    }

    private void setMarkerFromPreferences()
    {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        float lat = preferences.getFloat(Const.key_goal_lat, Const.key_goal_none);
        float lng = preferences.getFloat(Const.key_goal_lng, Const.key_goal_none);
        if(lat != Const.key_goal_none && lng != Const.key_goal_none)
        {
            drawMarker(new LatLng(
                    Double.parseDouble(String.valueOf(lat)),
                    Double.parseDouble(String.valueOf(lng))
            ));
        }
    }

    private void drawTrack(colorTrack track) {
        PolylineOptions options = new PolylineOptions();
        for (Point point : track.track) {
            LatLng location = new LatLng(point.getLatitude(), point.getLongitude());
            options.add(location);
            if(getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).getBoolean(Const.preferences_key_setting_draw_circles, false))
                drawCircle(location, track.color);
        }
        options.color(track.color);
        map.addPolyline(options);

        Log.d("Map", "Track " + track.id + " drawn");
    }

    private void setOnLongClickListener() {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.d("Map", "Executing OnMapLongClick");
                showActionsDialog(latLng);
            }
        });
        Log.d("Map", "Menu listener set");
    }

    private void setOnTrackClickListener() {
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Log.d("Map", "Polyline (track) clicked");
                int newcolor = polyline.getColor();
                newcolor = newcolor + 25;
                polyline.setColor(newcolor);
            }
        });
        Log.d("Map", "OnTrackClicked Listener set");
    }

    void drawCircle(LatLng position, int color) {
        CircleOptions options = new CircleOptions();
        options.radius(5.0);
        options.center(position);
        options.fillColor(color);
        map.addCircle(options);
    }

    void drawMarker(LatLng position) {
        MarkerOptions options = new MarkerOptions();
        options.position(position).draggable(false);

        goal_marker = map.addMarker(options);
        Log.d("Map", "Marker on map added " + position.latitude + "/" + position.longitude);
    }

    void navigateTo(LatLng goal) {

        if(goal_marker != null)
        {
            goal_marker.setPosition(goal);
        }
        else {
            drawMarker(goal);
        }
        setPreferencesNavigationGoal(goal);
    }

    private void setPreferencesNavigationGoal(LatLng latLng)
    {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putFloat(Const.key_goal_lat, Float.parseFloat(String.valueOf(latLng.latitude)));
        editor.putFloat(Const.key_goal_lng, Float.parseFloat(String.valueOf(latLng.longitude)));
        editor.apply();
    }

    private void resetPreferencesNavigationGoal()
    {
        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.remove(Const.key_goal_lat);
        editor.remove(Const.key_goal_lng);
        editor.apply();

        goal_marker.remove();
    }

    private void showActionsDialog(LatLng position) {
        new MapDialogBuilder(self, position).getActionsDialog().show();
    }

    private void showTrackList(LatLng position) {
        Cursor c = db.getTracksWithCursor();
        if (c.getCount() < 1) {
            Toast.makeText(this, "Brak tras do wyświetlenia", Toast.LENGTH_SHORT);
            return;
        }
        String[] names = new String[c.getCount()];
        boolean[] checked = new boolean[c.getCount()];
        int[] ids = new int[c.getCount()];


        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            ids[i] = c.getInt(c.getColumnIndex(db.KEY_ID));
            names[i] = c.getString(c.getColumnIndex(db.KEY_NAME));
            if (existsInList(ids[i]))
                checked[i] = true;
            else
                checked[i] = false;

            c.moveToNext();
        }


        new MapDialogBuilder(self, position).getTrackListDialog(ids, names, checked).show();
    }

    private boolean existsInList(int trackid) {
        for (colorTrack a : tracks
                ) {
            if (a.id == trackid)
                return true;
        }
        return false;
    }

    private class colorTrack {
        int id;
        ArrayList<Point> track;
        int color;

        void addPoint(Point point) {
            this.track.add(point);
        }
    }

    private class MapDialogBuilder extends AlertDialog {
        AlertDialog.Builder builder;
        LatLng position;

        MapDialogBuilder(Context context, LatLng pos) {
            super(context);
            builder = new AlertDialog.Builder(self);
            position = pos;
        }

        AlertDialog getActionsDialog() {
            return builder.setItems(R.array.actions, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            navigateTo(position);
                            dismiss();
                            break;
                        case 1:
                            resetPreferencesNavigationGoal();
                            dismiss();
                            break;
                        case 2:
                            dismiss();
                            showTrackList(position);
                            break;
                        default:
                            break;
                    }
                }
            }).create();
        }

        Dialog getTrackListDialog(final int[] ids, final String[] trackNames, final boolean[] checked) {
            return builder.setPositiveButton("OK", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dismiss();
                }
            }).
                    setMultiChoiceItems(trackNames, checked, new OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            if (b) {
                                addTrack(ids[i]);
                                checked[i] = true;
                            } else {
                                removeTrack(ids[i]);
                                checked[i] = false;
                            }
                            map.clear();
                            drawAll();
                        }
                    }).create();
        }
    }
}
