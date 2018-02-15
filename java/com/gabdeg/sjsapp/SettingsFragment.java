package com.gabdeg.sjsapp;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

/**
 * Created by ishan on 2/14/18.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.v("PreferenceFragment", "Running...");

        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

}
