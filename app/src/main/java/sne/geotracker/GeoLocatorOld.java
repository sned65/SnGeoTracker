package sne.geotracker;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Admin on 14.06.2016.
 */
public class GeoLocatorOld
{
    private static final String TAG = GeoLocatorOld.class.getName();

    private Service _service;
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

    public GeoLocatorOld(Service service)
    {
        _service = service;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_service);

        _accuracy = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_WRITE_ACCURACY, GeoService.DEFAULT_ACCURACY_THRESHOLD);
        _minTime = Mix.getLongPreference(prefs, SettingsFragment.PREFERENCE_MIN_TIME, GeoService.DEFAULT_MIN_TIME) * 1000;
        _minDistance = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_MIN_DISTANCE, GeoService.DEFAULT_MIN_DISTANCE);

        Set<String> default_providers = new HashSet<>();
        for (String p : _service.getResources().getStringArray(R.array.pref_providers))
        {
            default_providers.add(p);
        }
        _providers = prefs.getStringSet(SettingsFragment.PREFERENCE_PROVIDERS, default_providers);
    }

    public void onStartCommand()
    {
        if (ActivityCompat.checkSelfPermission(_service,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e(TAG, "No location permissions!");
            return;
        }

        // Acquire a reference to the system Location Manager
        _locationManager = (LocationManager) _service.getSystemService(Context.LOCATION_SERVICE);

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

        if (_providers != null && _providers.contains(_service.getString(R.string.provider_network)))
        {
            _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, _minTime, _minDistance, _locationListener);
        }
        if (_providers != null && _providers.contains(_service.getString(R.string.provider_gps)))
        {
            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _minTime, _minDistance, _locationListener);
        }
    }

    private void saveNewLocation(Location location)
    {
        //Log.i(TAG, "saveNewLocation() "+location);
        if (_accuracy != 0 && location.getAccuracy() > _accuracy) return;
        DatabaseHelper.getInstance(_service).addLocation(location);
    }

    public void stopUpdates()
    {
        if (_locationManager != null && _locationListener != null)
        {
            try
            {
                _locationManager.removeUpdates(_locationListener);
            }
            catch (SecurityException e)
            {
                Log.e(TAG, "stopUpdates() Cannot remove listener. "+e.getMessage());
            }
        }
    }
}
