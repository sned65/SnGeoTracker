package sne.geotracker;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends PreferenceFragment
{
    public static final String PREFERENCE_WRITE_ACCURACY = "pref_accuracy_threshold";
    public static final String PREFERENCE_ROUTE_ACCURACY = "pref_route_accuracy_threshold";
    public static final String PREFERENCE_MIN_TIME = "pref_min_time";
    public static final String PREFERENCE_MIN_DISTANCE = "pref_min_distance";
    public static final String PREFERENCE_PROVIDERS = "pref_providers";
    public static final String PREFERENCE_USE_FUSED = "pref_use_fused";
    public static final String PREFERENCE_MAP = "pref_map";
    public static final String PREFERENCE_SHOW_ALL_COLUMNS = "pref_show_all_columns";

    private static final String TAG = SettingsFragment.class.getSimpleName();

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     * <br/>
     * Note: The reference to the listener is stored in an instance data field of the object
     * that will exist as long as the listener is needed.
     */
    private static Preference.OnPreferenceChangeListener _prefChangeListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue)
        {
            setPreferenceSummary(preference, newValue);
//            String stringValue = newValue.toString();
//
//            if (preference instanceof ListPreference)
//            {
//                // For list preferences, look up the correct display value in
//                // the preference's 'entries' list.
//                ListPreference listPreference = (ListPreference) preference;
//                int index = listPreference.findIndexOfValue(stringValue);
//
//                // Set the summary to reflect the new value.
//                preference.setSummary(
//                        index >= 0
//                                ? listPreference.getEntries()[index]
//                                : null);
//
//            }
//            /*
//            else if (preference instanceof RingtonePreference)
//            {
//                // For ringtone preferences, look up the correct display value
//                // using RingtoneManager.
//                if (TextUtils.isEmpty(stringValue))
//                {
//                    // Empty values correspond to 'silent' (no ringtone).
//                    preference.setSummary(R.string.pref_ringtone_silent);
//
//                }
//                else
//                {
//                    Ringtone ringtone = RingtoneManager.getRingtone(
//                            preference.getContext(), Uri.parse(stringValue));
//
//                    if (ringtone == null)
//                    {
//                        // Clear the summary if there was a lookup error.
//                        preference.setSummary(null);
//                    }
//                    else
//                    {
//                        // Set the summary to reflect the new ringtone display
//                        // name.
//                        String name = ringtone.getTitle(preference.getContext());
//                        preference.setSummary(name);
//                    }
//                }
//
//            }
//            */
//            else
//            {
//                // For all other preferences, set the summary to the value's
//                // simple string representation.
//                preference.setSummary(stringValue);
//            }
            return true;
        }
    };

    private static void setPreferenceSummary(Preference preference, Object newValue)
    {
        String stringValue = newValue.toString();

        if (preference instanceof ListPreference)
        {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        }
            /*
            else if (preference instanceof RingtonePreference)
            {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue))
                {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                }
                else
                {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null)
                    {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    }
                    else
                    {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            }
            */
        else
        {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #_prefChangeListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference, Object defValue)
    {
        //Log.i(TAG, "bindPreferenceSummaryToValue() called: "+preference);
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(_prefChangeListener);

        // Trigger the listener immediately with the preference's
        // current value.

        if (preference instanceof MultiSelectListPreference)
        {
            _prefChangeListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getStringSet(preference.getKey(), (Set<String>) defValue));
        }
        else
        {
            String defValueStr;
            if (defValue == null)
            {
                defValueStr = "";
            }
            else
            {
                defValueStr = defValue.toString();
            }
            _prefChangeListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), defValueStr));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_all);

//        MultiSelectListPreference prefProviders = (MultiSelectListPreference) findPreference(PREFERENCE_PROVIDERS);
//        // all providers are initially selected
//        String[] allProviders = getResources().getStringArray(R.array.pref_providers);
//        Set<String> default_providers = new HashSet<>();
//        for (String p : allProviders)
//        {
//            default_providers.add(p);
//        }
//        prefProviders.setDefaultValue(allProviders);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        bindPreferenceSummaryToValue(findPreference(PREFERENCE_WRITE_ACCURACY), null);
        bindPreferenceSummaryToValue(findPreference(PREFERENCE_ROUTE_ACCURACY), null);
        bindPreferenceSummaryToValue(findPreference(PREFERENCE_MIN_TIME), null);
        bindPreferenceSummaryToValue(findPreference(PREFERENCE_MIN_DISTANCE), null);
        bindPreferenceSummaryToValue(findPreference(PREFERENCE_PROVIDERS), null);
        //bindPreferenceSummaryToValue(findPreference(PREFERENCE_PROVIDERS), default_providers);
        bindPreferenceSummaryToValue(findPreference(PREFERENCE_MAP), null);
    }
}
