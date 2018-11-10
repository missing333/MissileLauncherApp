package com.missilelauncher.missilelauncher;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import static android.preference.Preference.*;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    private static OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof NumberPickerPreference){
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary((Integer) value);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }


            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }



    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || Group1PreferenceFragment.class.getName().equals(fragmentName)
                || Group2PreferenceFragment.class.getName().equals(fragmentName)
                || Group3PreferenceFragment.class.getName().equals(fragmentName)
                || Group4PreferenceFragment.class.getName().equals(fragmentName)
                || Group5PreferenceFragment.class.getName().equals(fragmentName)
                || Group6PreferenceFragment.class.getName().equals(fragmentName)
                || Group7PreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        private SharedPreferences.OnSharedPreferenceChangeListener listener;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("numZones"));
            bindPreferenceSummaryToValue(findPreference("numAppCols"));
            bindPreferenceSummaryToValue(findPreference("numAppRows"));

            /////////////checking for Full key here////////////////
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    // Implementation
                    if (key.equals("numZones") && Integer.parseInt(prefs.getString("numZones","3")) > 3 ){
                        if (isProInstalled(getContext())){
                            Toast.makeText(getContext(), "Thanks for being PRO!!!!!!", Toast.LENGTH_SHORT).show();
                        }else{
                            prefs.edit().putString("numZones","3").commit();      //restore back down to 3 if they aren't Pro
                            startActivity(new Intent(getContext(), PlayStorePrompt.class));
                        }
                    }


                    //this stops and restarts the activation areas
                    Intent intent = null;
                    try {
                        intent = new Intent(getContext(), FloatingWindow.class);
                        intent.setAction("Stop");
                        getContext().stopService(intent);
                    } catch (Exception e) {
                    }

                    SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    intent.setAction("Start");
                    if (settingsPrefs.getBoolean("appEnabled", true)){
                        if(settingsPrefs.getBoolean("foregroundNotif", true)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Log.v("app", "Starting Foreground Service");
                                getContext().startForegroundService(intent);
                            } else {
                                Log.v("app", "Starting regular Service");
                                getContext().startService(intent);
                            }
                        }else {
                            Log.v("app", "Starting regular Service");
                            getContext().startService(intent);
                        }
                    }
                }
            };
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.registerOnSharedPreferenceChangeListener(listener);

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            prefs.unregisterOnSharedPreferenceChangeListener(listener);
        }

        protected boolean isProInstalled(Context context) {
            PackageManager manager = context.getPackageManager();
            if (manager.checkSignatures(context.getPackageName(), "com.missing.missilelauncherpro")
                    == PackageManager.SIGNATURE_MATCH) {
                //Pro key installed, and signatures match
                return true;
            }
            return false;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }else if (item.getTitle() == "Number of Groups"){
                Toast.makeText(getContext(), "Just picked numGroups!!!", Toast.LENGTH_SHORT).show();
            }
            return super.onOptionsItemSelected(item);
        }

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Group1PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_group1);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("groupName1"));
            bindPreferenceSummaryToValue(findPreference("sortG1"));

            Preference p1 = findPreference("group1AppList");
            p1.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),G1SelectedItems.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference icon = findPreference("group1Icon");
            icon.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),GroupIconPicker.class);
                    intent.putExtra("Group", 1);
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();


            Preference icon = findPreference("group1Icon");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                int id = (int) settingsPrefs.getLong("iconID1",R.drawable.ring_50dp );
                Drawable d = ContextCompat.getDrawable(getContext(), id);
                icon.setIcon(d);
            }
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Group2PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_group2);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("groupName2"));
            bindPreferenceSummaryToValue(findPreference("sortG2"));

            Preference p2 = findPreference("group2AppList");
            p2.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),G2SelectedItems.class);
                    startActivity(intent);
                    return false;
                }
            });
            Preference icon = findPreference("group2Icon");
            icon.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),GroupIconPicker.class);
                    intent.putExtra("Group", 2);
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();


            Preference icon = findPreference("group2Icon");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                int id = (int) settingsPrefs.getLong("iconID2",R.drawable.ring_50dp );
                Drawable d = ContextCompat.getDrawable(getContext(), id);
                icon.setIcon(d);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Group3PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_group3);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("groupName3"));
            bindPreferenceSummaryToValue(findPreference("sortG3"));

            Preference p3 = findPreference("group3AppList");
            p3.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),G3SelectedItems.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference icon = findPreference("group3Icon");
            icon.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),GroupIconPicker.class);
                    intent.putExtra("Group", 3);
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();


            Preference icon = findPreference("group3Icon");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                int id = (int) settingsPrefs.getLong("iconID3",R.drawable.ring_50dp );
                Drawable d = ContextCompat.getDrawable(getContext(), id);
                icon.setIcon(d);
            }
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Group4PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_group4);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("groupName4"));
            bindPreferenceSummaryToValue(findPreference("sortG4"));

            Preference p4 = findPreference("group4AppList");
            p4.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),G4SelectedItems.class);
                    startActivity(intent);
                    return false;
                }
            });
            Preference icon = findPreference("group4Icon");
            icon.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),GroupIconPicker.class);
                    intent.putExtra("Group", 4);
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();


            Preference icon = findPreference("group4Icon");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                int id = (int) settingsPrefs.getLong("iconID4",R.drawable.ring_50dp );
                Drawable d = ContextCompat.getDrawable(getContext(), id);
                icon.setIcon(d);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Group5PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_group5);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("groupName5"));
            bindPreferenceSummaryToValue(findPreference("sortG5"));

            Preference p5 = findPreference("group5AppList");
            p5.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),G5SelectedItems.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference icon = findPreference("group5Icon");
            icon.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),GroupIconPicker.class);
                    intent.putExtra("Group", 5);
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();


            Preference icon = findPreference("group5Icon");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                int id = (int) settingsPrefs.getLong("iconID5",R.drawable.ring_50dp );
                Drawable d = ContextCompat.getDrawable(getContext(), id);
                icon.setIcon(d);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Group6PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_group6);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("groupName6"));
            bindPreferenceSummaryToValue(findPreference("sortG6"));

            Preference p6 = findPreference("group6AppList");
            p6.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),G6SelectedItems.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference icon = findPreference("group6Icon");
            icon.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),GroupIconPicker.class);
                    intent.putExtra("Group", 6);
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();


            Preference icon = findPreference("group6Icon");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                int id = (int) settingsPrefs.getLong("iconID6",R.drawable.ring_50dp );
                Drawable d = ContextCompat.getDrawable(getContext(), id);
                icon.setIcon(d);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class Group7PreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_group7);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("groupName7"));
            bindPreferenceSummaryToValue(findPreference("sortG7"));

            Preference p7 = findPreference("group7AppList");
            p7.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),G7SelectedItems.class);
                    startActivity(intent);
                    return false;
                }
            });

            Preference icon = findPreference("group7Icon");
            icon.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(),GroupIconPicker.class);
                    intent.putExtra("Group", 7);
                    startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();


            Preference icon = findPreference("group7Icon");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                int id = (int) settingsPrefs.getLong("iconID7",R.drawable.ring_50dp );
                Drawable d = ContextCompat.getDrawable(getContext(), id);
                icon.setIcon(d);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
