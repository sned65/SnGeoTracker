package sne.geotracker.yandex;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

public class Polyline extends OverlayItem
{
    private List<GeoPoint> _geoPoints = new ArrayList<>();
    private List<ScreenPoint> _screenPoints = new ArrayList<>();

    public Polyline(GeoPoint geoPoint, Drawable drawable)
    {
        super(geoPoint, drawable);
    }

    public List<GeoPoint> getGeoPoints()
    {
        return _geoPoints;
    }

    public void addGeoPoint(GeoPoint point)
    {
        _geoPoints.add(point);
    }

    public List<ScreenPoint> getScreenPoints()
    {
        return _screenPoints;
    }

    public void addScreenPoint(ScreenPoint point)
    {
        _screenPoints.add(point);
    }

    @Override
    public int compareTo(Object another)
    {
        // SN strange feature of Yandex Map Kit
        return 0;
    }
}
