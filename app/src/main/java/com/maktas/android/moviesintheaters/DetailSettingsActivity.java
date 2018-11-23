package com.maktas.android.moviesintheaters;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by Mehmet Aktas on 27.11.2017.
 */
public class DetailSettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @SuppressWarnings("deprecation")
    @Override public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.detail_pref_screen);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_lang_key)));
    }

    @Override
    public void onPause(){

        super.onPause();
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        String value = o.toString();

        if(preference instanceof ListPreference) {

            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(value);
        }
        return true;

    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

    }

}
