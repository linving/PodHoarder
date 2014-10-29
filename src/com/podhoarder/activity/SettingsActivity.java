package com.podhoarder.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
	private boolean mPrefChanged;
    public final static String INTENT_RESULTS_ID = "prefChanged";
	
	@Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mPrefChanged = false;
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(spChanged);
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            Preference lastEpisodePreference = findPreference(Constants.SETTINGS_KEY_LASTEPISODE);
            Preference gridItemPreference = findPreference(Constants.SETTINGS_KEY_GRIDITEMSIZE);
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removePreference(lastEpisodePreference);
            preferenceScreen.removePreference(gridItemPreference);
        }
    }

    private void finishActivity()
    {
        Intent databackIntent = new Intent();
        databackIntent.putExtra(INTENT_RESULTS_ID, mPrefChanged);
        setResult(Activity.RESULT_OK, databackIntent);
        finish();
    }

    SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    mPrefChanged = true;
                }
                // your stuff here
            };

    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

}
