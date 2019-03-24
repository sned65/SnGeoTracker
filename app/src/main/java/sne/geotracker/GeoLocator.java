package sne.geotracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * This class provides geo-location services.
 * More modern approach to geo location comparing with old code in
 * {@link GeoService}, {@link GeoLocatorOld}, and {@link GeoLocatorFused}.
 */
public class GeoLocator
{
    private static final String TAG = GeoLocator.class.getName();

    private Context _context;
    private FusedLocationProviderClient _fusedLocationClient;
    private Location _lastLocation;

    public GeoLocator(Context context)
    {
        _context = context.getApplicationContext();
        initFusedLocationServices();
    }

    private void initFusedLocationServices()
    {
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(_context);
    }

    public void requestLastLocation()
    {
        if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // ACCESS_FINE_LOCATION is granted in the calling service
            return;
        }
        _fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>()
        {
            @Override
            public void onSuccess(Location location)
            {
                Log.d(TAG, "location = " + location);
                _lastLocation = location;
            }
        });
    }

    public Location getLastLocation()
    {
        return _lastLocation;
    }
}
