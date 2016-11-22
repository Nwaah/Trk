package nwaah.trk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Nwaah on 2016-05-09.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "Trk";

    // Table Names
    public static final String TABLE_POINT = "point";
    public static final String TABLE_TRACK = "track";
    //

    // Common column names
    public static final String KEY_ID = "_id";

    // POINT Table - column names
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";
    public static final String KEY_TIME = "time";
    public static final String KEY_TRACK = "track";

    // TRACK Table - column names
    public static final String KEY_NAME = "name";


    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_POINT = "CREATE TABLE IF NOT EXISTS "
            + TABLE_POINT + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_X
            + " REAL," + KEY_Y + " REAL," + KEY_TIME
            + " TEXT," + KEY_TRACK + " INTEGER" + ")";

    // Tag table create statement
    private static final String CREATE_TABLE_TRACK = "CREATE TABLE IF NOT EXISTS " + TABLE_TRACK
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT" + ")";
    private ArrayList<Track> tracks;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_POINT);
        db.execSQL(CREATE_TABLE_TRACK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);

        // create new tables
        onCreate(db);
    }
    public void reset() {
        onUpgrade(getWritableDatabase(), 1,1);
    }


    public Point[] getPoints(int trackid)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM "+TABLE_POINT+" WHERE track = "+trackid;

        Cursor c = db.rawQuery(q, null);
        if (c != null)
            c.moveToFirst();

        int size = c.getCount();
        Point[] res = new Point[size];

        for(int i=0; i<size; i++)
        {
            res[i] = new Point(
                    c.getDouble(c.getColumnIndex(KEY_X)),
                    c.getDouble(c.getColumnIndex(KEY_Y)),
                    c.getString(c.getColumnIndex(KEY_TIME)),
                    c.getInt(c.getColumnIndex(KEY_TRACK))
            );
            c.moveToNext();
        }
        c.close();
        db.close();
        return res;
    }

    public Cursor getPointsWithCursor(int trackid)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM "+TABLE_POINT+" WHERE track="+trackid;

        Cursor c = db.rawQuery(q, null);

        db.close();
        return c;
    }

    public Point getPoint(long id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM "+TABLE_POINT+" WHERE "+KEY_ID+"="+id;

        Cursor c = db.rawQuery(q, null);
        if (c != null)
            c.moveToFirst();

        double x = c.getDouble(c.getColumnIndex(KEY_X));
        double y = c.getDouble(c.getColumnIndex(KEY_Y));
        String time = c.getString(c.getColumnIndex(KEY_TIME));
        int track = c.getInt(c.getColumnIndex(KEY_TRACK));
        c.close();
        db.close();
        return new Point(x,y,time,track);
    }

    public long savePoint(Point point)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_X, point.getX());
        values.put(KEY_Y, point.getY());
        values.put(KEY_TIME, point.getTime());
        values.put(KEY_TRACK, point.getTrack());

        long id = db.insert(TABLE_POINT, null, values);
        db.close();

        return id;
    }

    public void deleteTrack(int i) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POINT, KEY_TRACK + " = ?", new String[]{String.valueOf(i)});
        db.delete(TABLE_TRACK, KEY_ID + " = ?", new String[]{String.valueOf(i)});
        db.close();
    }

    public Cursor getTracksWithCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM "+TABLE_TRACK;
        Cursor c = db.rawQuery(q, null);
        int count = c.getCount();
        db.close();


        return c;
    }

    public Track[] getTracks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM "+TABLE_TRACK;

        Cursor c = db.rawQuery(q, null);
        Track[] res = new Track[c.getCount()];
        for (int i=0; i<c.getCount(); i++)
        {
            res[i] = new Track(c.getInt(c.getColumnIndex(KEY_ID)), c.getString(c.getColumnIndex
                    (KEY_NAME)));

        }
    c.close();
        return res;
    }

    public long saveTrack(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues stuff = new ContentValues(1);
        stuff.put(KEY_NAME, name);
        long id = db.insert(TABLE_TRACK, null, stuff);

        db.close();
        return id;
    }

    public void renameTrack(long id, String newname)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues stuff = new ContentValues(1);
        stuff.put(KEY_NAME, newname);
        db.update(TABLE_TRACK, stuff, KEY_ID+" = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    public String getTrackStartDate(int trackId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM "+TABLE_TRACK+" WHERE "+KEY_ID+" = ?", new
                String[]{String.valueOf(trackId)});
        String date;
        if (c.getCount()<1)
            date="Brak daty";
        else {
            c.moveToFirst();
            date = c.getString(c.getColumnIndex(KEY_TIME));
        }
        c.close();
        db.close();
        return date;
    }

    public boolean clear()
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_TRACK, null, null);
            db.delete(TABLE_POINT, null, null);
            return true;
        }
        catch (Exception e){return false;}
    }

    public String getTrackDetails(int trackId)
    {
        return "Data rozpoczęcia: " +
                getTrackStartDate(trackId) +
                "\nCzas trwania: " +
                getTrackTime(trackId) +
                "\nDługość trasy: " +
                getTrackLength(trackId);
    }

    public double getTrackLength(int trackid)
    {
        Cursor c = this.getPointsWithCursor(trackid);
        c.moveToFirst();
        double startLatitude = c.getDouble(c.getColumnIndex(KEY_X));
        double startLongitude = c.getDouble(c.getColumnIndex(KEY_Y));
        double length=0;
        c.moveToNext();
        while (!c.isAfterLast())
        {
            float[] result = new float[1];
            double endLatitude = c.getDouble(c.getColumnIndex(KEY_X));
            double endLongitude = c.getDouble(c.getColumnIndex(KEY_Y));

            Location.distanceBetween(startLatitude, startLongitude, endLatitude,
                    endLongitude, result);

            length += result[0];

            startLatitude = endLatitude;
            startLongitude = endLongitude;
        }
        return length;
    }

    public int getTrackTime(int trackid)
    {
        Cursor c = this.getPointsWithCursor(trackid);
        c.moveToFirst();
        int first_time = Integer.getInteger(c.getString(c.getColumnIndex(KEY_TIME)));
        c.moveToLast();
        int last_time = Integer.getInteger(c.getString(c.getColumnIndex(KEY_TIME)));
        return last_time-first_time;
    }

}
