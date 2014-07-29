package com.podhoarderproject.podhoarder.activity;

import com.podhoarderproject.podhoarder.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
{
	public static String SETTINGS_KEY_DELETELISTENED = "pref_deleteListened";
	public static String SETTINGS_KEY_PLAYNEXTFILE = "pref_playNextFile";
	public static String SETTINGS_KEY_STARTTAB = "pref_startTab";
	public static String SETTINGS_KEY_GRIDSHOWTITLE = "pref_gridShowTitle";
	
	public static String SETTINGS_GROUP_KEY_PLAYER = "pref_player";
	public static String SETTINGS_GROUP_KEY_UI = "pref_ui";
	
	
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
        }
    }
}
