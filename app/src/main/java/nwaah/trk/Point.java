package nwaah.trk;

import java.util.Date;


public class Point {
    public double latitude;
    public double longitude;
    public double altitude;
    public String time;
    public int track;

    public Point(double latitude, double longitude, double altitude, String time, int track) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.time = time;
        this.track = track;
    }

    public Point(double latitude, double longitude, int track) {
        this(latitude, longitude, 0, new Date().toString(), track);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")[" + time + "]<" + track + ">";
    }
}
