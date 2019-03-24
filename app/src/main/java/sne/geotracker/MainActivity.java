package sne.geotracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getName();
    // permission request codes need to be < 256
    private static final int RC_PERMISSIONS = 2;

    private Chronometer _chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_all, false);

        _chronometer = (Chronometer) findViewById(R.id.chronometer);

        Button startBtn = (Button) findViewById(R.id.btn_start);
        startBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startTrackingChecked();
            }
        });

        Button stopBtn = (Button) findViewById(R.id.btn_stop);
        stopBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                stopTracking();
            }
        });

        Button showTableBtn = (Button) findViewById(R.id.btn_show_table);
        showTableBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showTable();
            }
        });

        Button showRouteBtn = (Button) findViewById(R.id.btn_show_route);
        showRouteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showRoute();
            }
        });

        Button homeRoadBtn = (Button) findViewById(R.id.btn_home_road);
        homeRoadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                homeRoad();
            }
        });

        // create/upgrade DB
        DatabaseHelper.getInstance(this);
    }

    @Override
    protected void onDestroy()
    {
        stopTracking();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.i(TAG, "onOptionsItemSelected(" + item.getItemId() + ") called");
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity1.class);
                startActivity(settings);
                return true;
            case R.id.menu_about:
                action_showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void action_showAbout()
    {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(getResources().getString(R.string.app_name)).append("</b><br/>");
        try
        {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            sb.append("Version ").append(pinfo.versionName).append("<br/>");
            sb.append("Version code ").append(pinfo.versionCode).append("<br/>");
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        msgBox.setMessage(Html.fromHtml(sb.toString()));
        msgBox.show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode != RC_PERMISSIONS)
        {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "Permissions granted");
            // we have permission, so start geo tracking
            startTracking();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.error_no_location_perm)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private void startTrackingChecked()
    {
        int permLocation = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permWrite = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permLocation == PackageManager.PERMISSION_GRANTED &&
                permWrite == PackageManager.PERMISSION_GRANTED)
        {
            startTracking();
        }
        else
        {
            final String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSIONS);
            return;
        }
    }

    private void startTracking()
    {
        DatabaseHelper.getInstance(this).truncate(); // maybe AsyncTask ?
        _chronometer.setBase(SystemClock.elapsedRealtime());
        _chronometer.setText("00:00");
        _chronometer.start();

        Intent gs = new Intent(this, GeoService.class);
        startService(gs);
    }

    private void stopTracking()
    {
        _chronometer.stop();
        Intent gs = new Intent(this, GeoService.class);
        stopService(gs);
    }

    private void showTable()
    {
        Intent sr = new Intent(this, LocationTableActivity.class);
        startActivity(sr);
    }

    private void showRoute()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String google = getString(R.string.map_google);
        String yandex = getString(R.string.map_yandex);
        String osm = getString(R.string.map_osm);
        String map_provider = prefs.getString(SettingsFragment.PREFERENCE_MAP, google);

        Intent route;
        if (map_provider.equals(yandex))
        {
            route = new Intent(this, RouteActivityYandex.class);
        }
        else if (map_provider.equals(osm))
        {
            route = new Intent(this, RouteActivityOSM.class);
        }
        else
        {
            route = new Intent(this, RouteActivityGoogle.class);
        }
        startActivity(route);
    }

    private void homeRoad()
    {
        Intent hr = new Intent(this, HomeRoadActivity.class);
        startActivity(hr);
    }
}
