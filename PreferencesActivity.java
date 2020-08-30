package com.example.earthquake_service_alarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;


public class PreferencesActivity extends AppCompatActivity {
    @Override  public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pereference_layout);
    }

    public static class PrefFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.userpreferences_xml, null);

            SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getContext());
            final EditTextPreference magnitudePref = (EditTextPreference)findPreference("PREF_MAGNITUDE");
            magnitudePref.setSummary(shp.getString("PREF_MAGNITUDE","3"));
            magnitudePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    magnitudePref.setSummary(newValue.toString());
                    magnitudePref.setDefaultValue(newValue);

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                }
            });

        }



    }


}
//            SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(getContext());
//            int index = Integer.parseInt(shp.getString("SWITCH_LANGUAGES", "0"));
//            String[] languageArray = getResources().getStringArray(R.array.language_options);
//            final ListPreference languageList = (ListPreference) findPreference("SWITCH_LANGUAGES");
//            languageList.setSummary(languageArray[index]);


//            languageList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    // TODO Auto-generated method stub
//                    String[] languageString = getResources().getStringArray(R.array.language_options);
//                    int index = Integer.parseInt(newValue.toString());
//                    languageList.setSummary(languageString[index]);
//                    languageList.setDefaultValue(languageString[index]);
//
//                    Intent intent = new Intent(getActivity(), MainActivity.class);
//                    //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivityForResult(intent, 0);
//                    return true;
//                }
//            });

//            int unitIndex = Integer.parseInt(shp.getString("SWITCH_UNIT", "0"));
//            String[] unitArray = getResources().getStringArray(R.array.AttitudeUnit_options);
//            final ListPreference unitList = (ListPreference) findPreference("SWITCH_UNIT");
//            unitList.setSummary(unitArray[unitIndex]);
//
//
//            unitList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(Preference preference, Object newValue) {
//                    // TODO Auto-generated method stub
//                    String[] unitString = getResources().getStringArray(R.array.AttitudeUnit_options);
//                    int unitIndex = Integer.parseInt(newValue.toString());
//                    unitList.setSummary(unitString[unitIndex]);
//                    unitList.setDefaultValue(unitString[unitIndex]);
//
////                    Intent intent = new Intent(getActivity(), MainActivity.class);
////                    //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
////                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
////                    startActivityForResult(intent, 0);
//                    return true;
//                }
//            });