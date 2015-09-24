package com.firstbuild.androidapp.paragon.settings;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firstbuild.androidapp.R;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        String menu = getIntent().getStringExtra("SelectedMenu");

        if(menu.equals("MenuSettings")){
            getFragmentManager().beginTransaction().replace(R.id.content_frame, new PreferenceSettings()).commit();
        }
        else{
            getFragmentManager().beginTransaction().replace(R.id.content_frame, new PreferenceAbout()).commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PreferenceSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        public static final String KEY_PREF_TEMP_UNIT = "pref_temp_unit";
        public static final String KEY_PREF_UNIT = "pref_unit";
        private String TAG = PreferenceSettings.class.getSimpleName();

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            Log.d(TAG, "onSharedPreferenceChanged " + s);

            Preference connectionPref = findPreference(s);
            // Set summary to be the user-description for the selected value
            connectionPref.setSummary(sharedPreferences.getString(s, ""));

        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }


    public static class PreferenceAbout extends PreferenceFragment {
        private String TAG = PreferenceSettings.class.getSimpleName();

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);


            Preference term_service = findPreference("pref_term_service");

            term_service.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(TAG, "onPreferenceClick pref_term_service");

                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.term_of_service_title)
                            .content(R.string.term_of_service_content)
                            .positiveText(R.string.term_of_service_ok)
                            .show();

                    return false;
                }
            });

            Preference opensource_license = findPreference("pref_licenses");

            opensource_license.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(TAG, "onPreferenceClick pref_term_service");

                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.opensource_license_title)
                            .content(R.string.opensource_license_content)
                            .positiveText(R.string.opensource_license_ok)
                            .show();

                    return false;
                }
            });

        }

//        @Override
//        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//            Log.d(TAG, "onSharedPreferenceChanged " + s);
//
//            Preference connectionPref = findPreference(s);
//            // Set summary to be the user-description for the selected value
//            connectionPref.setSummary(sharedPreferences.getString(s, ""));
//
//        }

        @Override
        public void onResume() {
            super.onResume();
//            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
//            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }


}
