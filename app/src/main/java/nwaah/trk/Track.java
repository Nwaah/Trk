package nwaah.trk;

/**
 * Created by Nwaah on 2016-05-09.
 */
public class Track {
    public int id;
    public String name;
    public String date;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return id+": "+name;
    }

    public Track(int id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
