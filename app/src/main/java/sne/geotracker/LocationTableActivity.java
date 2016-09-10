package sne.geotracker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Shows table with recorded activities
 */
public class LocationTableActivity extends AppCompatActivity
{
    private TableLayout _table;
    private SimpleDateFormat _sdf = new SimpleDateFormat("HH:mm:ss");
    private boolean _showAllCols;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_positions);

        TableLoaderTask tableLoaderTask = new TableLoaderTask(this);
        tableLoaderTask.execute();
    }

    private void changeVisibility()
    {
        LinearLayout layout = (LinearLayout) findViewById(R.id.tableLayout);
        layout.setVisibility(View.VISIBLE);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void makeTable(List<Pair<Location, DatabaseHelper.LocationData>> locations)
    {
        TextView titleView = (TextView) findViewById(R.id.tableTitle);

        if (locations.isEmpty())
        {
            String title = getString(R.string.no_locations);
            titleView.setText(title);
            changeVisibility();
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        _showAllCols = prefs.getBoolean(SettingsFragment.PREFERENCE_SHOW_ALL_COLUMNS, false);

        _table = (TableLayout) findViewById(R.id.tblLocations);

        headerColumnVisibility(R.id.hdrAltitude);
        headerColumnVisibility(R.id.hdrTime2);
        headerColumnVisibility(R.id.hdrSpeed);

        Location firstLoc = locations.get(0).getFirst();
        Location lastLoc = locations.get(locations.size()-1).getFirst();
        String title_1 = getString(R.string.location_table_title_1);
        String title_2 = getString(R.string.location_table_title_2);
        String title_3 = getString(R.string.location_table_title_3);
        String distance = getString(R.string.distance);
        String m = getString(R.string.m);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String first = sdf.format(new Date(firstLoc.getTime()));
        String last = sdf.format(new Date(lastLoc.getTime()));
        float path = pathLength(locations);

        String title = title_1 + locations.size()
                + title_2 + " " + first + " " + title_3 + " " + last
                + ". " + distance + String.format(" %.0f ", path) + m;
        titleView.setText(title);

        DatabaseHelper.LocationData data_prev = null;
        for (Pair<Location, DatabaseHelper.LocationData> loc : locations)
        {
            DatabaseHelper.LocationData data = loc.getSecond();
            if (data_prev != null && data.receivedOn < data_prev.receivedOn)
            {
                fillRow(loc, Color.RED);
            }
            else
            {
                fillRow(loc, Color.WHITE);
            }
            data_prev = data;
        }

        changeVisibility();
        //_table.requestLayout();
        //_table.forceLayout();
        //_table.invalidate();
    }

    private void headerColumnVisibility(@IdRes int id)
    {
        TextView hdr = (TextView) findViewById(id);
        if (_showAllCols)
        {
            hdr.setVisibility(View.VISIBLE);
        }
        else
        {
            hdr.setVisibility(View.GONE);
        }
    }

    private void fillRow(Pair<Location, DatabaseHelper.LocationData> loc_data, int bgColor)
    {
        Location loc = loc_data.getFirst();
        DatabaseHelper.LocationData data = loc_data.getSecond();

        TableRow.LayoutParams layoutProvider =
                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutProvider.gravity = Gravity.CENTER_HORIZONTAL;
        TextView providerText = new TextView(this);
        providerText.setText(loc.getProvider());
        providerText.setLayoutParams(layoutProvider);

//        TableRow.LayoutParams layoutRight =
//                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
//        layoutRight.gravity = Gravity.CENTER_HORIZONTAL;

        TextView latText = new TextView(this);
        String lat = String.format(Locale.US, "%.5f", loc.getLatitude());
        latText.setText(lat);
//        latText.setLayoutParams(layoutRight);

        TextView lngText = new TextView(this);
        String lng = String.format(Locale.US, "%.5f", loc.getLongitude());
        lngText.setText(lng);
//        lngText.setLayoutParams(layoutRight);

        TextView altText = new TextView(this);
        if (_showAllCols)
        {
            String alt = String.format(Locale.US, "%4.0f", loc.getAltitude());
            altText.setText(alt);
//            altText.setLayoutParams(layoutRight);
        }

        TextView accText = new TextView(this);
        String acc = String.format(Locale.US, "%.1f", loc.getAccuracy());
        accText.setText(acc);
//        accText.setLayoutParams(layoutRight);

        TextView timeText = new TextView(this);
        Date d = new Date(loc.getTime());
        String time = _sdf.format(d);
        timeText.setText(time);
//        timeText.setLayoutParams(layoutRight);

        TextView receivedOnText = new TextView(this);
        if (_showAllCols)
        {
            Date dr = new Date(data.receivedOn);
            String receivedOn = _sdf.format(dr);
            receivedOnText.setText(receivedOn);
//            receivedOnText.setLayoutParams(layoutRight);
        }

        TextView speedText = new TextView(this);
        if (_showAllCols)
        {
            String speed = String.format(Locale.US, "%.2f", loc.getSpeed() * 3.6);
            speedText.setText(speed);
//            speedText.setLayoutParams(layoutRight);
        }

        TableRow row = new TableRow(this);
        row.setBackgroundColor(bgColor);

        row.addView(providerText);
        row.addView(latText);
        row.addView(lngText);
        if (_showAllCols)
        {
            row.addView(altText);
        }
        row.addView(accText);
        row.addView(timeText);
        if (_showAllCols)
        {
            row.addView(receivedOnText);
            row.addView(speedText);
        }

        _table.addView(row);
    }

    private float pathLength(List<Pair<Location, DatabaseHelper.LocationData>> locations)
    {
        float path = 0;
        if (locations.size() < 2) return path;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float accuracy = Mix.getFloatPreference(prefs, SettingsFragment.PREFERENCE_ROUTE_ACCURACY, GeoService.DEFAULT_ROUTE_ACCURACY);

        for (int i = 1; i < locations.size(); ++i)
        {
            if (accuracy > 0 && locations.get(i).getFirst().getAccuracy() > accuracy) continue;

            Location prev = locations.get(i-1).getFirst();
            Location curr = locations.get(i).getFirst();
            float delta = curr.distanceTo(prev);
            path += delta;
        }

        return path;
    }

    ///////////////////////////////////////////////////////////////
    private class TableLoaderTask extends AsyncTask<Void, Void, List<Pair<Location, DatabaseHelper.LocationData>>>
    {
        private Activity _activity;
        private String _orderError;

        public TableLoaderTask(Activity activity)
        {
            _activity = activity;
        }

        @Override
        protected List<Pair<Location, DatabaseHelper.LocationData>> doInBackground(Void... voids)
        {
            DatabaseHelper db = DatabaseHelper.getInstance(_activity);
            List<Pair<Location, DatabaseHelper.LocationData>> locations = db.getLocations(0);
            _orderError = db.getOrderError();
            return locations;
        }

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected void onPostExecute(List<Pair<Location, DatabaseHelper.LocationData>> result)
        {
            makeTable(result);

            if (_orderError != null)
            {
                Toast.makeText(_activity, _orderError, Toast.LENGTH_LONG).show();
            }
        }

    }
}
