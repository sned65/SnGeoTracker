package sne.geotracker;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import sne.geotracker.yandex.OverlayRect;

/**
 * Shows route using Yandex maps.
 */
public class RouteActivityYandex extends AppCompatActivity
{
    private static final float DEFAULT_MAP_ZOOM = 15.f;

    private List<Pair<Location, DatabaseHelper.LocationData>> _locations;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_yandex);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float accuracy = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_ROUTE_ACCURACY, GeoService.DEFAULT_ROUTE_ACCURACY);

        _locations = DatabaseHelper.getInstance(this).getLocations(accuracy);

        final MapView mapView = (MapView) findViewById(R.id.map_yandex);
        //mapView.showBuiltInScreenButtons(true);

        MapController mapController = mapView.getMapController();
        mapController.setZoomCurrent(DEFAULT_MAP_ZOOM);

        OverlayManager overlayManager = mapController.getOverlayManager();
        // Disable determining the user's location
        overlayManager.getMyLocation().setEnabled(false);

        OverlayRect overlayRect = new OverlayRect(mapController, _locations);
        mapController.getOverlayManager().addOverlay(overlayRect);

        if (_locations != null && _locations.size() > 0)
        {
            Location first = _locations.get(0).getFirst();

            Overlay overlay = new Overlay(mapController);
            GeoPoint gp = new GeoPoint(first.getLatitude(), first.getLongitude());
            Drawable here = getResources().getDrawable(R.drawable.here, null);
            OverlayItem start = new OverlayItem(gp, here);
            overlay.addOverlayItem(start);
            mapController.getOverlayManager().addOverlay(overlay);

            mapController.setPositionAnimationTo(gp);
        }
    }
}
