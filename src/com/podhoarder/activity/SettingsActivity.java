package com.podhoarder.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.podhoarder.util.Constants;
import com.podhoarderproject.podhoarder.R;

public class SettingsActivity extends PreferenceActivity
{
	
	
	@Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            Preference lastEpisodePreference = findPreference(Constants.SETTINGS_KEY_LASTEPISODE);
            Preference lastFilterPreference = findPreference(Constants.SETTINGS_KEY_LASTFILTER);
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removePreference(lastEpisodePreference);
            preferenceScreen.removePreference(lastFilterPreference);
        }
    }
}
