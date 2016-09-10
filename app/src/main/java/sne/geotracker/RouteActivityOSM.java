package sne.geotracker;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows route using Open Street maps.
 */
public class RouteActivityOSM extends AppCompatActivity
{
    // permission request codes need to be < 256
    private static final int RC_PERMISSIONS = 102;

    private static final int DEFAULT_MAP_ZOOM = 17;
    private static final double MOSCOW_LAT = 55.751999;
    private static final double MOSCOW_LNG = 37.617734;

    private float _accuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_osm);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        _accuracy = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_ROUTE_ACCURACY, GeoService.DEFAULT_ROUTE_ACCURACY);

        // Request permissions to support Android Marshmallow and above devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkPermissions();
        }
        else
        {
            makeMap();
        }
    }

    private void checkPermissions()
    {
        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty())
        {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, RC_PERMISSIONS);
        }
        else
        {
            // We already have permissions, so handle as normal
            makeMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
        case RC_PERMISSIONS:
        {
            Map<String, Integer> perms = new HashMap<>();
            // Initial
            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            // Fill with results
            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);
            // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
            Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (location && storage)
            {
                // All Permissions Granted
                makeMap();
            }
            else if (location)
            {
                Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
            }
            else if (storage)
            {
                Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
            }
            else
            { // !location && !storage case
                // Permission Denied
                Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                        "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
            }
        }
        break;
        default:
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void makeMap()
    {
        //important! set your user agent to prevent getting banned from the osm servers
        OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);

        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // add default zoom buttons, and ability to zoom with 2 fingers (multi-touch)
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        // Set zoom

        IMapController mapController = map.getController();
        mapController.setZoom(DEFAULT_MAP_ZOOM);

        // Get root
        List<Pair<Location, DatabaseHelper.LocationData>> locations = DatabaseHelper.getInstance(this).getLocations(_accuracy);
        if (locations.isEmpty())
        {
            GeoPoint startPoint = new GeoPoint(MOSCOW_LAT, MOSCOW_LNG);
            mapController.setCenter(startPoint);
            return;
        }

        // move the map on a default view point
        Location loc0 = locations.get(0).getFirst();
        GeoPoint startPoint = new GeoPoint(loc0);
        mapController.setCenter(startPoint);

        // Route
        Polyline line = new Polyline(this);
        line.setTitle("Central Park, NYC");
        line.setSubDescription(Polyline.class.getCanonicalName());
        line.setWidth(2f);

        List<GeoPoint> pts = new ArrayList<>();
        for (Pair<Location, DatabaseHelper.LocationData> loc_pair : locations)
        {
            Location loc = loc_pair.getFirst();
            GeoPoint p = new GeoPoint(loc);
            pts.add(p);
        }
        line.setPoints(pts);

        line.setGeodesic(true);
        line.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble, map));
        //Note, the info window will not show if you set the onclick listener
        //line can also attach click listeners to the line
//        line.setOnClickListener(new Polyline.OnClickListener() {
//            @Override
//            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
//                Toast.makeText(context, "Hello world!", Toast.LENGTH_LONG).show();
//                return false;
//            }
//        });
        map.getOverlayManager().add(line);
    }
}
