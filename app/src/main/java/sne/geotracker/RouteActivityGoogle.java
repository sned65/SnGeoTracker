package sne.geotracker;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Shows route using Google maps.
 */
public class RouteActivityGoogle extends AppCompatActivity implements OnMapReadyCallback
{
    private static final float DEFAULT_MAP_ZOOM = 17.f;

    private float _accuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_google);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        _accuracy = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_ROUTE_ACCURACY, GeoService.DEFAULT_ROUTE_ACCURACY);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); // calls onMapReady() asynchronously
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near client location.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        List<Pair<Location, DatabaseHelper.LocationData>> locations = DatabaseHelper.getInstance(this).getLocations(_accuracy);
        if (locations.isEmpty()) return;

        Location loc0 = locations.get(0).getFirst();
        LatLng start = new LatLng(loc0.getLatitude(), loc0.getLongitude());
        CameraPosition camera_pos = new CameraPosition(start, DEFAULT_MAP_ZOOM, 0, 0);
        CameraUpdate updPos = CameraUpdateFactory.newCameraPosition(camera_pos);
        googleMap.moveCamera(updPos);

        MarkerOptions m_opt = new MarkerOptions().position(start).title(loc0.getProvider());
        googleMap.addMarker(m_opt);

//        for (Location loc : locations)
//        {
//            LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
//            MarkerOptions opt = new MarkerOptions().position(pos).title(loc.getProvider());
//            googleMap.addMarker(opt);
//        }

        PolylineOptions opt = new PolylineOptions().geodesic(true);
        for (Pair<Location, DatabaseHelper.LocationData> loc_d : locations)
        {
            Location loc = loc_d.getFirst();
            LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
            opt.add(pos);
        }
        googleMap.addPolyline(opt);
    }
}
