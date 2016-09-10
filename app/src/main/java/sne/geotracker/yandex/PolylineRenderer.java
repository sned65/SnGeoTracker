package sne.geotracker.yandex;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import ru.yandex.yandexmapkit.overlay.IRender;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

public class PolylineRenderer implements IRender
{
    @Override
    public void draw(Canvas canvas, OverlayItem item_)
    {
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        Polyline item = (Polyline) item_;
        Path p = new Path();
        if (item.getScreenPoints() != null && item.getScreenPoints().size() > 0)
        {
            ScreenPoint screenPoint = item.getScreenPoints().get(0);
            p.moveTo(screenPoint.getX(), screenPoint.getY());

            for (int i = 1; i < item.getScreenPoints().size(); i++)
            {
                screenPoint = item.getScreenPoints().get(i);
                p.lineTo(screenPoint.getX(), screenPoint.getY());
            }
            canvas.drawPath(p, mPaint);
        }
    }
}
