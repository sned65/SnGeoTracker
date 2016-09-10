package sne.geotracker;

import android.content.SharedPreferences;
import android.location.Location;

import java.util.Objects;

/**
 * Collection of utilities
 */
public class Mix
{
    private Mix()
    {
    }

    public static float getFloatPreference(SharedPreferences prefs, String key, float defaultValue)
    {
        String p = prefs.getString(key, String.valueOf(defaultValue));
        if (p.isEmpty()) return defaultValue;
        return Float.valueOf(p);
    }

    public static long getLongPreference(SharedPreferences prefs, String key, long defaultValue)
    {
        String p = prefs.getString(key, String.valueOf(defaultValue));
        if (p.isEmpty()) return defaultValue;
        return Long.valueOf(p);
    }

    /**
     * Determines whether one {@code Location} reading is better than the current {@code Location} fix.
     * From <a href="https://developer.android.com/guide/topics/location/strategies.html">
     * Location Strategies</a>.
     *
     * @param location            The new {@code Location} that you want to evaluate
     * @param currentBestLocation The current {@code Location} fix, to which you want to compare the new one
     */
    public boolean isBetterLocation(Location location, Location currentBestLocation)
    {
        if (currentBestLocation == null)
        {
            // A new location is always better than no location
            return true;
        }

        final int TWO_MINUTES = 1000 * 60 * 2;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer)
        {
            return true;
            // If the new location is more than two minutes older, it must be worse
        }
        else if (isSignificantlyOlder)
        {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = Objects.equals(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate)
        {
            return true;
        }
        else if (isNewer && !isLessAccurate)
        {
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
        {
            return true;
        }
        return false;
    }
}
