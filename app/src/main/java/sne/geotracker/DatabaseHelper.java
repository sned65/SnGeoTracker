package sne.geotracker;

import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Wraps up the logic to create and upgrade a database.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = DatabaseHelper.class.getName();
    private static final String DATABASE_NAME = "sn_geotracker.db";
    private static final int SCHEMA_VERSION = 1;

    private static final String GEO_TBL = "locations";
    private static final String GEO_COL_ID = "id";
    private static final String GEO_COL_LAT = "latitude";
    private static final String GEO_COL_LNG = "longitude";
    private static final String GEO_COL_ALT = "altitude";
    private static final String GEO_COL_SPEED = "speed";
    private static final String GEO_COL_ACC = "accuracy";
    private static final String GEO_COL_PROVIDER = "provider";
    private static final String GEO_COL_LOC_TIME = "located_on";
    private static final String GEO_COL_RECEIVED_ON = "received_on";

    private static DatabaseHelper _instance;
    private String _orderError = null;

    public static DatabaseHelper getInstance(Service service)
    {
        if (_instance == null)
        {
            _instance = new DatabaseHelper(service.getApplicationContext());
        }
        return _instance;
    }

    public static DatabaseHelper getInstance(Activity service)
    {
        if (_instance == null)
        {
            _instance = new DatabaseHelper(service.getApplicationContext());
        }
        return _instance;
    }

    private DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.i(TAG, "onCreate() called");
        db.execSQL("CREATE TABLE " + GEO_TBL + "(" +
                GEO_COL_ID + " INTEGER PRIMARY KEY ON CONFLICT FAIL AUTOINCREMENT, " +
                GEO_COL_LAT + " REAL, " +
                GEO_COL_LNG + " REAL, " +
                GEO_COL_ALT + " REAL, " +
                GEO_COL_ACC + " REAL, " +
                GEO_COL_SPEED + " REAL, " +
                GEO_COL_PROVIDER + " TEXT, " +
                GEO_COL_LOC_TIME + " INTEGER, " +
                GEO_COL_RECEIVED_ON + " INTEGER " +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new RuntimeException("How did we get here?");
    }

    public void truncate()
    {
        Log.i(TAG, "truncate() called");
        SQLiteDatabase db = getWritableDatabase();
        String sql = "DELETE FROM " + GEO_TBL;
        db.execSQL(sql);
    }

    public void addLocation(Location location)
    {
        Log.i(TAG, "addLocation("+location+") called");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(GEO_COL_LAT, location.getLatitude());
        cv.put(GEO_COL_LNG, location.getLongitude());
        cv.put(GEO_COL_ALT, location.getAltitude());
        cv.put(GEO_COL_ACC, location.getAccuracy());
        cv.put(GEO_COL_SPEED, location.getSpeed());
        cv.put(GEO_COL_PROVIDER, location.getProvider());
        cv.put(GEO_COL_LOC_TIME, location.getTime());
        cv.put(GEO_COL_RECEIVED_ON, (new Date()).getTime());
        db.insert(GEO_TBL, null, cv);
    }

    public List<Pair<Location, LocationData>> getLocations(float accuracyThreshold)
    {
        List<Pair<Location, LocationData>> locations = new ArrayList<>();
        String sql =
                "SELECT " + GEO_COL_LAT + ", " + GEO_COL_LNG + ", " + GEO_COL_ALT + ", "
                        + GEO_COL_ACC + ", " + GEO_COL_SPEED + ", "
                        + GEO_COL_PROVIDER + ", " + GEO_COL_LOC_TIME
                        + ", " + GEO_COL_ID + ", " + GEO_COL_RECEIVED_ON
                        + " FROM " + GEO_TBL + " ORDER BY " + GEO_COL_LOC_TIME;
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        _orderError = null;
        long id_prev = -1;
        while (c.moveToNext())
        {
            float acc = c.getFloat(3);
            if (accuracyThreshold > 0 && acc > accuracyThreshold) continue;
            double lat = c.getDouble(0);
            double lng = c.getDouble(1);
            double alt = c.getDouble(2);
            float speed = c.getFloat(4);
            String provider = c.getString(5);
            long time = c.getLong(6);
            long receivedOn = c.getLong(8);
            long id = c.getLong(7);
            if (id < id_prev)
            {
                _orderError = "ID < ID_PREV: "+id+" < "+id_prev;
            }
            id_prev = id;

            Location pos = new Location(provider);
            pos.setAccuracy(acc);
            pos.setLatitude(lat);
            pos.setLongitude(lng);
            pos.setAltitude(alt);
            pos.setSpeed(speed);
            pos.setTime(time);

            LocationData data = new LocationData();
            data.id = id;
            data.receivedOn = receivedOn;

            locations.add(new Pair<>(pos, data));
        }
        c.close();
        return locations;
    }

    public String getOrderError()
    {
        return _orderError;
    }

    public class LocationData
    {
        public long id;
        public long receivedOn;
    }
}
