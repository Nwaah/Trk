package nwaah.trk;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class HeightChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_chart);
    }

    @Override
    protected void onStart() {
        super.onStart();

        int trackId = getIntent().getIntExtra(Const.current_track_id, 0);
        if (trackId > 0) {
            if (!createChart(new DatabaseHelper(this).getPoints(trackId))) {
                displayError();
            }
        }
    }

    private void displayError() {
        findViewById(R.id.chartError).setVisibility(View.VISIBLE);
    }

    private boolean createChart(ArrayList<Point> points) {
        ImageView imageView = (ImageView) findViewById(R.id.chartImage);
        try {
            ArrayList<HeightDist> heights = new ArrayList<>();
            double distance = 0;

            for (int i = 0; i < points.size(); i++) {
                if (i == 0)
                    distance += addToList(points.get(i), null, distance, heights);
                else
                    distance += addToList(points.get(i), points.get(i - 1), distance, heights);
            }

            imageView.setImageBitmap(createBMP(heights));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Bitmap createBMP(ArrayList<HeightDist> heights) {
        Bitmap img = Bitmap.createBitmap(Integer.parseInt(String.valueOf(heights.get(heights.size() - 1).dist)), 200, Bitmap.Config.RGB_565);
        for (HeightDist point : heights
                ) {
            img.prepareToDraw();
            img.setPixel(Integer.parseInt(String.valueOf(point.dist)), Integer.parseInt(String.valueOf(point.height)), Color.RED);
        }
        return img;
    }

    private double addToList(Point current, Point last, double distance, ArrayList<HeightDist> list) {
        HeightDist item = new HeightDist(current.altitude, distance);
        list.add(item);
        float[] result = new float[1];
        if (last != null) {
            Location.distanceBetween(current.latitude, current.longitude, last.latitude, last.longitude, result);
            if (result[0] != 0.0)
                return Double.parseDouble(String.valueOf(result[0]));
            else return 0;
        } else
            return 0;
    }

    private class HeightDist {
        double height;
        double dist;

        public HeightDist(double height, double dist) {
            this.height = height;
            this.dist = dist;
        }
    }
}
