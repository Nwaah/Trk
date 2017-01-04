package nwaah.trk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    int currentTrackId;
    int requestedTrackId;
    DatabaseHelper db;
    LocalBroadcastManager localBroadcastManager;
    BroadcastReceiver pointAddedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = new DatabaseHelper(this);
        currentTrackId = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE).getInt(Const.current_track_id, 0);
        requestedTrackId = getIntent().getIntExtra(Const.key_id, 0);

        if (requestedTrackId > 0) {
            TextView name = (TextView) findViewById(R.id.track_name);
            TextView details = (TextView) findViewById(R.id.track_details);

            name.setText(db.getTrackName(requestedTrackId));
            details.setText(db.getTrackExtendedDetails(requestedTrackId));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (requestedTrackId == currentTrackId) {
            localBroadcastManager = LocalBroadcastManager.getInstance(this);
            pointAddedReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    TextView details = (TextView) findViewById(R.id.track_details);
                    details.setText(db.getTrackExtendedDetails(currentTrackId));
                }
            };

            localBroadcastManager.registerReceiver(pointAddedReceiver, new IntentFilter(Const.intent_filter_action_refresh_views));
        }
    }

    @Override
    protected void onPause() {
        if (requestedTrackId == currentTrackId)
        localBroadcastManager.unregisterReceiver(pointAddedReceiver);
        super.onPause();
    }
}
