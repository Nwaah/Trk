package nwaah.trk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.GpsStatus;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TrackChoose extends AppCompatActivity{

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
        try {
            loadList();
        }
        catch (Exception e){
            Toast.makeText(this,"Nie udało się załadować tras! "+e.getMessage(),Toast.LENGTH_LONG)
                    .show();}
    }



    private void loadList() {
        final DatabaseHelper db = new DatabaseHelper(this);
        Cursor c = db.getTracksWithCursor();
        String[] from = {DatabaseHelper.KEY_ID, DatabaseHelper.KEY_NAME};
        int[] to = {R.id.track_item_id ,R.id.track_item_name};
        c.moveToFirst();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.track_list_item, c,
                from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        db.close();
        ListView list = (ListView)findViewById(R.id.track_list);
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
                TextView id_txt = (TextView)view.findViewById(R.id.track_item_id);
                TextView name_txt = (TextView)view.findViewById(R.id.track_item_name);
                final int arg_id = Integer.parseInt(id_txt.getText().toString());
                final String arg_name = name_txt.getText().toString();

                PopupMenu popup = new PopupMenu(self, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.track_actions, popup.getMenu());

                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                int action = item.getItemId();
                                switch (action)
                                {
                                    case R.id.track_rename:
                                        Intent intent = new Intent(self, RenameTrack.class);
                                        intent.putExtra(DatabaseHelper.KEY_ID, arg_id);
                                        intent.putExtra(DatabaseHelper.KEY_NAME, arg_name);
                                        startActivity(intent);
                                        return true;
                                    case R.id.track_delete:
                                        new DatabaseHelper(self).deleteTrack(arg_id);
                                        loadList();
                                        Toast.makeText(self, "Usunięto trasę "+arg_name, Toast
                                                .LENGTH_SHORT).show();
                                        return true;
                                    default:
                                        return false;
                            }
                        }});

                popup.show();
                return true;
            }
        });


    }


    public void setCurrentTrack(View currentTrack) {
        TextView id_txt = (TextView)currentTrack.findViewById(R.id.track_item_id);
        TextView name_txt = (TextView)currentTrack.findViewById(R.id.track_item_name);
        int id = Integer.parseInt(id_txt.getText().toString());
        String name = name_txt.getText().toString();
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
        Toast.makeText(this, "Trasa zmieniona", Toast.LENGTH_SHORT).show();
        stopService(new Intent(this, TrackingService.class));
    }

    public void exit()
    {
        this.onBackPressed();
    }

    void add()
    {
        Intent intent = new Intent(this, AddTrack.class);
        startActivity(intent);
        exit();
    }
}
