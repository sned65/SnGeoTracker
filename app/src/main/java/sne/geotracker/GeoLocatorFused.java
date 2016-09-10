package sne.geotracker;

import android.Manifest;
import android.app.Service;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Admin on 14.06.2016.
 */
public class GeoLocatorFused
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = GeoLocatorFused.class.getName();

    private Service _service;
    private GoogleApiClient _googleApiClient;

    private LocationListener _locationListener;
    private LocationRequest _locationRequest;
    // maximum accuracy of location, in meters
    private float _accuracy;
    // minimum time interval between location updates, in seconds
    private long _minTime;
    // minimum distance between location updates, in meters
    private float _minDistance;

    public GeoLocatorFused(Service service)
    {
        _service = service;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_service);
        _accuracy = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_WRITE_ACCURACY, GeoService.DEFAULT_ACCURACY_THRESHOLD);
        _minTime = Mix.getLongPreference(prefs, SettingsFragment.PREFERENCE_MIN_TIME, GeoService.DEFAULT_MIN_TIME) * 1000;
        _minDistance = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_MIN_DISTANCE, GeoService.DEFAULT_MIN_DISTANCE);

        initFusedLocationServices();
    }

    public void stopUpdates()
    {
        try
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    _googleApiClient, _locationListener);
        }
        catch (Exception e)
        {
            Log.e(TAG, "stopUpdates() "+e.getMessage());
        }
    }

    public void onStartCommand()
    {
        _googleApiClient.connect();
    }

    private void initFusedLocationServices()
    {
        _googleApiClient =
                (new GoogleApiClient.Builder(_service))
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

        _locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                saveNewLocation(location);
            }
        };

        _locationRequest = (new LocationRequest())
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1000)
        ;

        if (_minTime > 0)
        {
            _locationRequest.setInterval(_minTime);
        }
        else
        {
            _locationRequest.setInterval(1000);
        }
//        request=new LocationRequest()
//                .setNumUpdates(1)
//                .setExpirationDuration(60000)
//                .setInterval(1000)
//                .setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if (ActivityCompat.checkSelfPermission(_service,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(_googleApiClient, _locationRequest, _locationListener);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.w(TAG, "onConnectionSuspended(" + i + ") called, whatever that means");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Log.e(TAG, "onConnectionFailed() "+connectionResult);
    }

    private void saveNewLocation(Location location)
    {
        //Log.i(TAG, "saveNewLocation() "+location);
        if (_accuracy != 0 && location.getAccuracy() > _accuracy) return;
        DatabaseHelper.getInstance(_service).addLocation(location);
    }
}
