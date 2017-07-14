package com.quaap.fishberserker;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

/**
 * Created by tom on 2/10/17.
 * <p>
 * Copyright (C) 2017  tom
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
public class SettingsActivity  extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getListView().setBackgroundColor(Color.WHITE);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getInstance(this).getSoundEffects().playBGMusic(0);
    }

    @Override
    protected void onPause() {
        App.getInstance(this).getSoundEffects().playBGMusic(0);
        super.onPause();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (fragmentName.startsWith("com.quaap.fishberserker.SettingsActivity$")) {
            return true;
        }
        return super.isValidFragment(fragmentName);
    }




    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

        }

    }


}
