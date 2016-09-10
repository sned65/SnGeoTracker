package sne.geotracker.yandex;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import sne.geotracker.DatabaseHelper;
import sne.geotracker.Pair;
import sne.geotracker.R;

/**
 * Created by Admin on 28.06.2016.
 */
public class OverlayRect extends Overlay
{
    private Context _context;
    private Polyline _polyline;

    public OverlayRect(MapController mapController, List<Pair<Location, DatabaseHelper.LocationData>> locations)
    {
        super(mapController);
        _context = mapController.getContext();

        setIRender(new PolylineRenderer());

        _polyline = new Polyline(new GeoPoint(0, 0), _context.getResources().getDrawable(R.drawable.star, null));
        fillPolyline(locations);
        addOverlayItem(_polyline);
    }

    private void fillPolyline(List<Pair<Location, DatabaseHelper.LocationData>> locations)
    {
        for (Pair<Location, DatabaseHelper.LocationData> loc_d : locations)
        {
            Location loc = loc_d.getFirst();
            _polyline.addGeoPoint(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
        }
    }

    @Override
    public List<OverlayItem> prepareDraw()
    {
        List<OverlayItem> draw = new ArrayList<>();
        _polyline.getScreenPoints().clear();
        for (GeoPoint point : _polyline.getGeoPoints())
        {
            _polyline.addScreenPoint(getMapController().getScreenPoint(point));
        }
        draw.add(_polyline);

        return draw;
    }

    @Override
    public int compareTo(Object another)
    {
        // SN strange feature of Yandex Map Kit
        return 0;
    }
}
