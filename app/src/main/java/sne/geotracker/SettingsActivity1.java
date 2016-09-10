package sne.geotracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * Settings are presented as a single list.
 */
public class SettingsActivity1 extends AppCompatActivity
{

    private static final String TAG = SettingsActivity1.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
