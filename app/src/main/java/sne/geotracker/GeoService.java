package sne.geotracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Handles geolocation information.
 */
public class GeoService extends Service
{
    private static final String TAG = GeoService.class.getName();
    static final float DEFAULT_ACCURACY_THRESHOLD = 50.f;
    static final long DEFAULT_MIN_TIME = 5;
    static final float DEFAULT_MIN_DISTANCE = 10.f;
    public static final float DEFAULT_ROUTE_ACCURACY = 0;

    private boolean _useFused = false;
    private GeoLocatorOld _geoLocatorOld;
    private GeoLocatorFused _geoLocatorFused;

    private LocationManager _locationManager;
    private LocationListener _locationListener;

    // maximum accuracy of location, in meters
    private float _accuracy;
    // minimum time interval between location updates, in seconds
    private long _minTime;
    // minimum distance between location updates, in meters
    private float _minDistance;
    // providers
    private Set<String> _providers;

    @Override
    public void onCreate()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        _useFused = prefs.getBoolean(SettingsFragment.PREFERENCE_USE_FUSED, false);
        if (_useFused)
        {
            _geoLocatorFused = new GeoLocatorFused(this);
        }
        else
        {
            _geoLocatorOld = new GeoLocatorOld(this);
        }

        _accuracy = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_WRITE_ACCURACY, DEFAULT_ACCURACY_THRESHOLD);
        _minTime = Mix.getLongPreference(prefs, SettingsFragment.PREFERENCE_MIN_TIME, DEFAULT_MIN_TIME) * 1000;
        _minDistance = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_MIN_DISTANCE, DEFAULT_MIN_DISTANCE);

        Set<String> default_providers = new HashSet<>();
        for (String p : getResources().getStringArray(R.array.pref_providers))
        {
            default_providers.add(p);
        }
        _providers = prefs.getStringSet(SettingsFragment.PREFERENCE_PROVIDERS, default_providers);

        Log.i(TAG, "onCreate() _accuracy = "+_accuracy+", _minTime = "+_minTime+", _minDistance = "+_minDistance+", _providers = "+_providers);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Log.i(TAG, "onStartCommand() called");
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e(TAG, "No location permissions!");
            return START_NOT_STICKY;
        }

        if (_useFused)
        {
            //onStartCommandFusedGeo();
            _geoLocatorFused.onStartCommand();
        }
        else
        {
            //onStartCommandOldGeo();
            _geoLocatorOld.onStartCommand();
        }
        return START_STICKY;

/*

        // Acquire a reference to the system Location Manager
        _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        _locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                // Called when a new location is found by the location provider(s).
                saveNewLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates


        if (_providers != null && _providers.contains(getString(R.string.provider_network)))
        {
            _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, _minTime, _minDistance, _locationListener);
        }
        if (_providers != null && _providers.contains(getString(R.string.provider_gps)))
        {
            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _minTime, _minDistance, _locationListener);
        }
        return START_STICKY;
*/
    }


/*
    private void onStartCommandOldGeo()
    {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e(TAG, "No location permissions!");
            return;
        }

        // Acquire a reference to the system Location Manager
        _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        _locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                // Called when a new location is found by the location provider(s).
                saveNewLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates

        if (_providers != null && _providers.contains(getString(R.string.provider_network)))
        {
            _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, _minTime, _minDistance, _locationListener);
        }
        if (_providers != null && _providers.contains(getString(R.string.provider_gps)))
        {
            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _minTime, _minDistance, _locationListener);
        }
    }
*/

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "onDestroy() called");
        if (_geoLocatorOld != null)
        {
            _geoLocatorOld.stopUpdates();
        }
        if (_geoLocatorFused != null)
        {
            _geoLocatorFused.stopUpdates();
        }
//        if (_locationManager != null && _locationListener != null)
//        {
//            try
//            {
//                _locationManager.removeUpdates(_locationListener);
//            }
//            catch (SecurityException e)
//            {
//                Log.e(TAG, "onDestroy() Cannot remove listener. "+e.getMessage());
//            }
//        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

//    private void saveNewLocation(Location location)
//    {
//        //Log.i(TAG, "saveNewLocation() "+location);
//        if (_accuracy != 0 && location.getAccuracy() > _accuracy) return;
//        DatabaseHelper.getInstance(this).addLocation(location);
//    }
}
