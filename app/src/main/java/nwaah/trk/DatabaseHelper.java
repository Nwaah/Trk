package nwaah.trk;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Nwaah on 2016-05-09.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Name
    public static final String DATABASE_NAME = "Trk";
    // Table Names
    public static final String TABLE_POINT = "point";
    public static final String TABLE_TRACK = "track";
    // Common column names
    public static final String KEY_ID = "_id";
    // POINT Table - column names
    public static final String KEY_LATITUDE = "latitude";
    //
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ALTITUDE = "altitude";
    public static final String KEY_TIME = "time";
    public static final String KEY_TRACK = "track";
    public static final String KEY_COLOR = "color";
    public static final String KEY_START_DATE = "start_date";
    // TRACK Table - column names
    public static final String KEY_NAME = "name";
    // Logcat tag
    private static final String LOG = "DatabaseHelper";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Table Create Statements
    private static final String CREATE_TABLE_POINT = "CREATE TABLE IF NOT EXISTS "
            + TABLE_POINT + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LATITUDE
            + " REAL," + KEY_LONGITUDE + " REAL," + KEY_ALTITUDE + " REAL," + KEY_TIME
            + " TEXT," + KEY_TRACK + " INTEGER" + ")";

    // Tag table create statement
    private static final String CREATE_TABLE_TRACK = "CREATE TABLE IF NOT EXISTS " + TABLE_TRACK
            + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_COLOR + " INTEGER," + KEY_START_DATE + " TEXT " + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_POINT);
        db.execSQL(CREATE_TABLE_TRACK);

        Log.d("Database", "Tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POINT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACK);

        // create new tables
        onCreate(db);
        Log.d("Database", "Tables reset");
    }

    public void reset() {
        onUpgrade(getWritableDatabase(), 1, 1);
    }


    public ArrayList<Point> getPoints(int trackid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM " + TABLE_POINT + " WHERE track = " + trackid;

        Cursor c = db.rawQuery(q, null);
        if (c != null)
            c.moveToFirst();

        int size = c.getCount();
        ArrayList<Point> res = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            res.add(new Point(
                            c.getDouble(c.getColumnIndex(KEY_LATITUDE)),
                            c.getDouble(c.getColumnIndex(KEY_LONGITUDE)),
                            c.getDouble(c.getColumnIndex(KEY_ALTITUDE)),
                            c.getString(c.getColumnIndex(KEY_TIME)),
                            c.getInt(c.getColumnIndex(KEY_TRACK))
                    )
            );
            c.moveToNext();
        }
        c.close();
        db.close();
        Log.d("Database", "getPoints (" + trackid + ")");
        return res;
    }

    public Cursor getPointsWithCursor(int trackid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM " + TABLE_POINT + " WHERE track=" + trackid;
        Log.d("Database", "getPointsCursor (" + trackid + ")");
        return db.rawQuery(q, null);
    }

    public Point getPoint(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM " + TABLE_POINT + " WHERE " + KEY_ID + "=" + id;

        Cursor c = db.rawQuery(q, null);
        if (c != null)
            c.moveToFirst();

        double x = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
        double y = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));
        double alt = c.getDouble(c.getColumnIndex(KEY_ALTITUDE));
        String time = c.getString(c.getColumnIndex(KEY_TIME));
        int track = c.getInt(c.getColumnIndex(KEY_TRACK));
        c.close();
        db.close();
        Log.d("Database", "getPoint (" + id + ")");
        return new Point(x, y, alt, time, track);
    }

    public long savePoint(Point point) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LATITUDE, point.getLatitude());
        values.put(KEY_LONGITUDE, point.getLongitude());
        values.put(KEY_TIME, point.getTime());
        values.put(KEY_TRACK, point.getTrack());
        values.put(KEY_ALTITUDE, point.altitude);

        long id = db.insert(TABLE_POINT, null, values);

        db.close();
        verifyStartDate(point.getTrack(), point.getTime());

        Log.d("Database", "Point " + point.toString() + " added");
        return id;
    }

    private void verifyStartDate(int trackId, String time) {
        if (trackId == 0)
            return;

        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT " + KEY_START_DATE + " FROM " + TABLE_TRACK + " WHERE " + KEY_ID + " = " + trackId;
        boolean dateisempty = false;

        Cursor c = db.rawQuery(q, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            String date = c.getString(c.getColumnIndex(KEY_START_DATE));
            if (date.equals(""))
                dateisempty = true;
        }

        c.close();
        db.close();

        if (dateisempty) {
            db = this.getWritableDatabase();
            ContentValues stuff = new ContentValues();
            stuff.put(KEY_START_DATE, formatDate(Long.parseLong(time)));
            db.update(TABLE_TRACK, stuff, KEY_ID + " = ?", new String[]{String.valueOf(trackId)});
            db.close();
            Log.d("Database", "Starting date for track " + trackId + " - " + time + " added");
        }
    }

    public void deleteTrack(int i) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POINT, KEY_TRACK + " = ?", new String[]{String.valueOf(i)});
        db.delete(TABLE_TRACK, KEY_ID + " = ?", new String[]{String.valueOf(i)});
        db.close();
        Log.d("Database", "Track " + i + " deleted");
    }

    public Cursor getTracksWithCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM " + TABLE_TRACK;
        Log.d("Database", "getTracksCursor (" + ")");
        return db.rawQuery(q, null);
    }

    public Track[] getTracks() {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT * FROM " + TABLE_TRACK;

        Cursor c = db.rawQuery(q, null);
        Track[] res = new Track[c.getCount()];
        for (int i = 0; i < c.getCount(); i++) {
            res[i] = new Track(c.getInt(c.getColumnIndex(KEY_ID)), c.getString(c.getColumnIndex
                    (KEY_NAME)));

        }
        c.close();
        return res;
    }

    public long saveTrack(String name) {
        return saveTrack(name, 0);
    }

    public long saveTrack(String name, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues stuff = new ContentValues(2);
        stuff.put(KEY_NAME, name);
        stuff.put(KEY_COLOR, color);
        stuff.put(KEY_START_DATE, "");
        long id = db.insert(TABLE_TRACK, null, stuff);

        db.close();
        Log.d("Database", "New track " + name + " added");
        return id;
    }


    public void renameTrack(long id, String newname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues stuff = new ContentValues(1);
        stuff.put(KEY_NAME, newname);
        db.update(TABLE_TRACK, stuff, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    public void editTrack(long id, String newname, int newColor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues stuff = new ContentValues(2);
        stuff.put(KEY_NAME, newname);
        stuff.put(KEY_COLOR, newColor);
        db.update(TABLE_TRACK, stuff, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        db.close();
    }

    public String getTrackStartDate(int trackId) {
        Cursor c = getPointsWithCursor(trackId);
        return getTrackStartDate(c);
    }

    public String getTrackStartDate(Cursor c) {
        String date;
        if (c.getCount() < 1)
            date = "0";
        else {
            c.moveToFirst();
            date = c.getString(c.getColumnIndex(KEY_TIME));
        }
        Log.d("Database", "getTrackStartDate = " + date);
        return date;
    }


    public boolean clear() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_TRACK, null, null);
            db.delete(TABLE_POINT, null, null);
            Log.d("Database", "Tables deleted");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getTrackDetails(int trackId) {
        if (trackId == 0)
            return "Brak trasy";
        else {
            Cursor track = getPointsWithCursor(trackId);
            String txt =
                    "Czas trwania: " +
                    formatTime(getTrackTime(track)) +
                    "\nDługość trasy: " +
                    formatDistance(getTrackLength(track)) +
                    "\nPołożenie: " +
                    getLastPosition(track);
            track.close();
            Log.d("Database", "getTrackDetails(" + trackId + ")");
            return txt;
        }
    }

    public String getLastPosition(Cursor track) {
        if (track.getCount()<1)
            return "Brak danych";
        track.moveToLast();
        int longitude = track.getInt(track.getColumnIndex(KEY_LONGITUDE));
        int latitude = track.getInt(track.getColumnIndex(KEY_LATITUDE));
        String result = "";
        if(latitude>=0)
            result += latitude + "°N";
        else result += -latitude + "°S";
        result += " ";
        if(longitude>=0)
            result += longitude + "°E";
        else result += -longitude + "°W";
        return result;
    }

    public String formatDistance(double trackLength) {
        long meters = (long)trackLength;
        double km = meters /100;
        km /= 10;
        return ""+ km + " km";
    }

    public String formatTime(long trackTime) {
        long hours = trackTime / (1000 * 60 * 60);
        trackTime -= hours * 1000 * 60 * 60;
        long minutes = trackTime / (1000 * 60);
        trackTime -= minutes * 1000 * 60;
        long seconds = trackTime / 1000;

        String result = "";
        if(hours > 0)
            result += hours + "h, ";
        if(minutes > 0 || hours > 0)
            result += minutes + "min, ";
        result += seconds + "s ";
        return result;
    }

    public String formatDate(long date) {
        if(date == 0)
            return "Brak daty";
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(date);

        String monthtxt;
        int month = calendar.get(Calendar.MONTH)+1;
        if(month<10)
            monthtxt = "0"+month;
        else
            monthtxt = ""+month;

        String daytxt;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if(day<10)
            daytxt= "0"+day;
        else
            daytxt=""+day;

        String hourtxt;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour<10)
            hourtxt = "0"+hour;
        else
            hourtxt = ""+hour;

        String minutetxt;
        int minute = calendar.get(Calendar.MINUTE);
        if(minute<10)
            minutetxt = "0"+minute;
        else
            minutetxt = ""+minute;

        return calendar.get(Calendar.YEAR) + "-"
                + monthtxt + "-"
                + daytxt + " "
                + hourtxt + ":" + minutetxt;
    }

    public double getTrackLength(int trackid) {
        if (trackid == 0)
            return 0;
        Cursor c = this.getPointsWithCursor(trackid);
        if (c.getCount() < 1)
            return 0;
        c.moveToFirst();
        double startLatitude = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
        double startLongitude = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));
        double length = 0;
        c.moveToNext();
        while (!c.isAfterLast()) {
            float[] result = new float[1];
            double endLatitude = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
            double endLongitude = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));

            Location.distanceBetween(startLatitude, startLongitude, endLatitude,
                    endLongitude, result);

            length += result[0];

            startLatitude = endLatitude;
            startLongitude = endLongitude;
        }
        c.close();
        return length;
    }

    public double getTrackLength(Cursor c) {
        if (c.getCount() < 2)
            return 0;
        c.moveToFirst();
        double startLatitude = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
        double startLongitude = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));
        double length = 0;
        c.moveToNext();
        while (!c.isAfterLast()) {
            float[] result = new float[1];
            double endLatitude = c.getDouble(c.getColumnIndex(KEY_LATITUDE));
            double endLongitude = c.getDouble(c.getColumnIndex(KEY_LONGITUDE));

            Location.distanceBetween(startLatitude, startLongitude, endLatitude,
                    endLongitude, result);

            length += result[0];

            startLatitude = endLatitude;
            startLongitude = endLongitude;
            c.moveToNext();
        }
        Log.d("Database", "getTrackLength = " + length);

        return length;
    }

    public long getTrackTime(int trackid) {
        if (trackid == 0)
            return 0;
        Cursor c = this.getPointsWithCursor(trackid);
        if (c.getCount() < 1)
            return 0;
        c.moveToFirst();
        long first_time = Long.parseLong(
                c.getString(
                        c.getColumnIndex(KEY_TIME)
                )
        );
        c.moveToLast();
        long last_time = Long.parseLong(
                c.getString(
                        c.getColumnIndex(KEY_TIME)
                )
        );
        c.close();
        return last_time - first_time;
    }

    public long getTrackTime(Cursor c) {
        if (c.getCount() < 1)
            return 0;
        c.moveToFirst();
        long first_time = Long.parseLong(
                c.getString(
                        c.getColumnIndex(KEY_TIME)
                )
        );
        c.moveToLast();
        long last_time = Long.parseLong(
                c.getString(
                        c.getColumnIndex(KEY_TIME)
                )
        );
        Log.d("Database", "getTrackTime = " + (last_time - first_time));

        return last_time - first_time;
    }

    public int getTrackColor(int trackid) {
        if (trackid == 0)
            return 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT " + KEY_COLOR + " FROM " + TABLE_TRACK + " WHERE " + KEY_ID + " = " + trackid;
        int color = 0;

        Cursor c = db.rawQuery(q, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            color = c.getInt(c.getColumnIndex(KEY_COLOR));
        }
        c.close();

        return color;
    }

    public String getTrackName(int trackid) {
        if (trackid == 0)
            return "Brak nazwy";
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "SELECT " + KEY_NAME + " FROM " + TABLE_TRACK + " WHERE " + KEY_ID + " = " + trackid;
        String name = "";

        Cursor c = db.rawQuery(q, null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            name = c.getString(c.getColumnIndex(KEY_NAME));
        }

        c.close();

        return name;
    }

    public float getDistanceToGoal(int trackId, float goalLat, float goalLng)
    {
        Cursor c = getPointsWithCursor(trackId);
        if(c.getCount()<1)
            return 0;
        c.moveToLast();
        float[] results = new float[1];
        Location.distanceBetween(c.getDouble(c.getColumnIndex(KEY_LATITUDE)), c.getDouble(c.getColumnIndex(KEY_LONGITUDE)), goalLat, goalLng, results);
        return results[0];
    }

    public String getTrackExtendedDetails(int currentTrackId) {
        //TODO implement getTrackExtendedDetails
        //DootDoot
        String result = "";
        Cursor currentTrack = getPointsWithCursor(currentTrackId);

        result += "Data rozpoczęcia: " + formatDate(Long.parseLong(getTrackStartDate(currentTrackId))) +"\n";
        result += "Przebyta odległość: " + formatDistance(getTrackLength(currentTrack)) +"\n";
        result += "Czas trwania: " + formatTime(getTrackTime(currentTrackId)) +"\n";
        result += "Liczba pomiarów: " + currentTrack.getCount() +"\n";
        result += "Ostatnie położenie: " + getLastPosition(currentTrack) +"\n";
        result += "Średnia prędkość: " + getAverageSpeed(currentTrack) + "\n";

        return result;
    }

    private String getAverageSpeed(Cursor currentTrack) {
        if(currentTrack.getCount() < 2)
            return "Brak danych";
        double speed = getTrackLength(currentTrack)/(getTrackTime(currentTrack)/1000); // result is in m/s
        speed *= 3.6;
        String spd = String.valueOf(speed).substring(0, 4);
        if(spd.charAt(3) == '.')
            spd = spd.substring(0, 3);
        return "" + spd + " km/h";
    }
}
