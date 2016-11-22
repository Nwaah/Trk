package nwaah.trk;

import java.util.Date;

/**
 * Created by Nwaah on 2016-05-09.
 */
public class Point {
    public double x;
    public double y;
    public String time;
    public int track;

    public Point(double x, double y, String time, int track)
    {
        this.x = x;
        this.y = y;
        this.time = time;
        this.track = track;
    }

    public Point(double x, double y, int track)
    {
        this(x,y,new Date().toString(),track);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getTime() {
        return time;
    }

    public int getTrack() {
        return track;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    @Override
    public String toString() {
        return "("+x+", "+y+")["+time+"]<"+track+">";
    }
}
