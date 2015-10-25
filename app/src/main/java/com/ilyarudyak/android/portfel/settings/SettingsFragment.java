package com.ilyarudyak.android.portfel.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ilyarudyak.android.portfel.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);

    }
}
