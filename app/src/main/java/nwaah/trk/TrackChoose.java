package nwaah.trk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TrackChoose extends AppCompatActivity {

    TrackChoose self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_choose);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        self = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            loadList();
        } catch (Exception e) {
            Log.d("TrackChoose", "Cannot display track list");
            Log.d("TrackChoose", e.getMessage());
        }
    }

    private void loadList() {
        final DatabaseHelper db = new DatabaseHelper(this);
        Cursor c = db.getTracksWithCursor();
        String[] from = {DatabaseHelper.KEY_ID, DatabaseHelper.KEY_NAME, DatabaseHelper.KEY_COLOR, DatabaseHelper.KEY_START_DATE};
        int[] to = {R.id.track_item_id, R.id.track_item_name, R.id.track_item_color, R.id.track_item_date};

        c.moveToFirst();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.track_list_item, c,
                from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        db.close();
        ListView list = (ListView) findViewById(R.id.track_list);
        list.setAdapter(adapter);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCurrentTrack(view);
                exit();
            }
        });


        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                TextView id_txt = (TextView) view.findViewById(R.id.track_item_id);
                final int arg_id = Integer.parseInt(id_txt.getText().toString());

                PopupMenu popup = new PopupMenu(self, view);
                final MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.track_actions, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int action = item.getItemId();
                        switch (action) {
                            case R.id.track_rename:
                                Intent intent = new Intent(self, EditTrack.class);
                                intent.putExtra(DatabaseHelper.KEY_ID, arg_id);
                                startActivity(intent);
                                return true;
                            case R.id.track_delete:
                                new DatabaseHelper(self).deleteTrack(arg_id);
                                SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
                                if (prefs.getInt(Const.current_track_id, 0) == arg_id)
                                    self.resetCurrentTrack(prefs);
                                loadList();
                                Toast.makeText(self, "Trasa usunięta", Toast
                                        .LENGTH_SHORT).show();
                                return true;
                            case R.id.track_show_on_map:
                                Intent mapIntent = new Intent(self, MapsActivity.class);
                                mapIntent.putExtra(Const.key_id, arg_id);
                                startActivity(mapIntent);
                                return true;
                            case R.id.track_details:
                                Intent detailsIntent = new Intent(self, DetailsActivity.class);
                                detailsIntent.putExtra(Const.key_id, arg_id);
                                startActivity(detailsIntent);
                            default:
                                return false;
                        }
                    }
                });


                popup.show();
                return true;
            }
        });
    }

    private void resetCurrentTrack(SharedPreferences preferences) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Const.current_track_id);
        editor.putInt(Const.current_track_id, 0);
        editor.apply();
    }

    public void setCurrentTrack(View currentTrack) {
        TextView id_txt = (TextView) currentTrack.findViewById(R.id.track_item_id);
        int id = Integer.parseInt(id_txt.getText().toString());
        SharedPreferences preferences = this.getSharedPreferences(getString(R.string.app_name),
                MODE_PRIVATE);
        SharedPreferences.Editor ed = preferences.edit();
        ed.remove(Const.current_track_id);
        ed.apply();
        ed.putInt(Const.current_track_id, id);
        ed.apply();
        Toast.makeText(this, "Trasa zmieniona", Toast.LENGTH_SHORT).show();
        stopService(new Intent(this, TrackingService.class));
    }

    public void exit() {
        this.onBackPressed();
    }

    void add() {
        Intent intent = new Intent(this, AddTrack.class);
        startActivity(intent);
        exit();
    }
}
