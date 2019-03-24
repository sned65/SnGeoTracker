package sne.geotracker;

import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class HomeRoadActivity extends AppCompatActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = HomeRoadActivity.class.getName();

    private static final String[] CLOCK = { "|", "/", "-", "\\" };

    private static final int PERIOD = 10 * 1000;
    private static final double HOME_LAT = 54.8773772;
    private static final double HOME_LNG = 37.211683;
    private static final double WORK_LAT = 55.6969869;
    private static final double WORK_LNG = 37.623453;

    private GeoLocator _geoLocator;
    private Handler _handler;
    private TextView _txtLocation;
    private TextView _txtDistToHome;
    private TextView _txtDistToWork;

    private TextView _txtClock;
    private int _clock = 0;

    private Runnable _runnable = new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                _geoLocator.requestLastLocation();
                Location current = _geoLocator.getLastLocation();
                updateUI(current);
            }
            finally
            {
                _handler.postDelayed(_runnable, PERIOD);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_road);

        _geoLocator = new GeoLocator(this);
        _geoLocator.requestLastLocation();

        _handler = new Handler();

        _txtClock = findViewById(R.id.txtClock);
        _txtLocation = findViewById(R.id.txtLocation);
        _txtDistToHome = findViewById(R.id.txtToHome);
        _txtDistToWork = findViewById(R.id.txtToWork);
    }

    @Override
    protected void onPause()
    {
        stopRunning();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        startRunning();
    }

    private void startRunning()
    {
        _runnable.run();
    }

    private void stopRunning()
    {
        _handler.removeCallbacks(_runnable);
    }

    private void updateUI(Location current)
    {
        _clock = (_clock + 1) % CLOCK.length;
        _txtClock.setText(CLOCK[_clock]);

        if (current == null)
        {
            _txtLocation.setText(R.string.na);
            _txtDistToHome.setText(R.string.na);
            _txtDistToWork.setText(R.string.na);
            return;
        }

        double lat = current.getLatitude();
        double lng = current.getLongitude();
        double acc = current.getAccuracy() / 1000.;
        //Log.i(TAG, "("+lat+", "+lng+")");
        String locationStr = String.format(Locale.US, "%.4f, %.4f", lat, lng);
        _txtLocation.setText(locationStr);

        float[] distanceHome = new float[2];
        Location.distanceBetween(lat, lng, HOME_LAT, HOME_LNG, distanceHome);
        String distHomeStr = getString(R.string.distance_format, distanceHome[0]/1000, acc);
        _txtDistToHome.setText(distHomeStr);

        float[] distanceWork = new float[2];
        Location.distanceBetween(lat, lng, WORK_LAT, WORK_LNG, distanceWork);
        String distWorkStr = getString(R.string.distance_format, distanceWork[0]/1000, acc);
        _txtDistToWork.setText(distWorkStr);
    }
}
