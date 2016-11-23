package nwaah.trk;

/**
 * Created by Nwaah on 2016-05-09.
 */
public class Track {
    public int id;
    public String name;
    public String date;
    public int color;

    public Track(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Track() {

    }

    @Override
    public String toString() {
        return id + ": " + name;
    }
}
