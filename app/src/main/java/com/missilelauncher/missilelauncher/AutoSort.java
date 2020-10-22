package com.missilelauncher.missilelauncher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutoSort extends AppCompatActivity {

    private ProgressBar progressBar;
    TextView txt;
    Integer count =1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("This will overwrite all of the Groups.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // do something
                    new FetchCategoryTask().execute();
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // do nothing (will close dialog)
                    finish();
                })
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private class FetchCategoryTask extends AsyncTask<ArrayList<AppInfo>[], Integer, ArrayList<AppInfo>[]> {

        private PackageManager pm;
        private int Other=1, Reading=2, Productivity=3, Games=4, Media=5, Social=6, Lifestyle=7;

        private String getCategoryNew(String package_name, Context applicationContext) {

            pm = getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = pm.getApplicationInfo(package_name, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int appCategory = 0;
                if (applicationInfo != null) {
                    appCategory = applicationInfo.category;
                }
                return (String) ApplicationInfo.getCategoryTitle(applicationContext, appCategory);
            }
            return "error getting category";

        }





        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setContentView(R.layout.activity_auto_sort);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            txt = (TextView) findViewById(R.id.output);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            int width = dm.widthPixels;
            int height = dm.heightPixels;

            getWindow().setLayout((int) (width * .7), (int) (height * .4));

            txt.setText(R.string.TaskStart);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int percent = count*100/getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA).size();
            String s = percent + "%";
            txt.setText(s);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected ArrayList[] doInBackground(ArrayList[]... arrayLists) {
            String category;
            pm = getPackageManager();

            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            Iterator<ApplicationInfo> iterator = packages.iterator();
            ArrayList[] groupAppList = new ArrayList[10];
            groupAppList[Games] = new ArrayList<>(0);
            groupAppList[Social] = new ArrayList<>(0);
            groupAppList[Media] = new ArrayList<>(0);
            groupAppList[Reading] = new ArrayList<>(0);
            groupAppList[Productivity] = new ArrayList<>(0);
            groupAppList[Other] = new ArrayList<>(0);
            groupAppList[Lifestyle] = new ArrayList<>(0);

            progressBar.setMax(packages.size());

            while (iterator.hasNext()) {
                count++;
                ApplicationInfo packageInfo = iterator.next();

                if (getPackageManager().getLaunchIntentForPackage(packageInfo.packageName) != null){

                    category = getCategoryNew(packageInfo.packageName, getApplicationContext());

                    Log.d("autosort",packageInfo.packageName + ": " + category);

                    AppInfo newInfo = new AppInfo();
                    newInfo.icon = packageInfo.loadIcon(getPackageManager());
                    newInfo.setPackageName(packageInfo.packageName);
                    newInfo.setLabel(packageInfo.loadLabel(getPackageManager()).toString());
                    newInfo.setLaunchIntent(getPackageManager().getLaunchIntentForPackage(packageInfo.packageName));

                    if (category == null){
                        groupAppList[Other].add(newInfo);
                    } else {
                        // store category or do something else
                        switch (category){
                            case "Arcade":
                            case "Card":
                            case "Puzzle":
                            case "Casual":
                            case "Racing":
                            case "Sport":
                            case "Action":
                            case "Adventure":
                            case "Board":
                            case "Casino":
                            case "Educational":
                            case "Music":
                            case "Role Playing":
                            case "Simulation":
                            case "Strategy":
                            case "Trivia":
                            case "Word Games":
                            case "Games":
                            case "Sports":
                                groupAppList[Games].add(newInfo);
                                break;
                            case "Entertainment":
                            case "Photography":
                            case "Video Players & Editors":
                            case "Music & Audio":
                            case "Movies & Video":
                            case "Photos & Images":
                                groupAppList[Media].add(newInfo);
                                break;
                            case "News & Magazines":
                            case "Comics":
                            case "Books & Reference":
                                groupAppList[Reading].add(newInfo);
                                break;
                            case "Finance":
                            case "Productivity":
                            case "Business":
                                groupAppList[Productivity].add(newInfo);
                                break;
                            case "Social":
                            case "Dating":
                            case "Communication":
                            case "Social & Communication":
                                groupAppList[Social].add(newInfo);
                                break;
                            case "Health & Fitness":
                            case "Shopping":
                            case "Weather":
                            case "Lifestyle":
                            case "Travel & Local":
                            case "House & Home":
                            case "Food & Drink":
                            case "Maps & Navigation":
                                groupAppList[Lifestyle].add(newInfo);
                                break;

                            case "Tools":
                            default:
                                groupAppList[Other].add(newInfo);
                                break;
                        }
                    }

                    publishProgress(count);
                }
            }

            return groupAppList;
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo>[] arrayList) {
            super.onPostExecute(arrayList);
            new G1SelectedItems().setG1Apps(arrayList[Other]);
            new G2SelectedItems().setG2Apps(arrayList[Reading]);
            new G3SelectedItems().setG3Apps(arrayList[Productivity]);
            new G4SelectedItems().setG4Apps(arrayList[Games]);
            new G5SelectedItems().setG5Apps(arrayList[Media]);
            new G6SelectedItems().setG6Apps(arrayList[Social]);
            new G7SelectedItems().setG7Apps(arrayList[Lifestyle]);

            //set lists of apps
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(),getAllPackageNames(arrayList[Other]), Other);
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(),getAllPackageNames(arrayList[Reading]), Reading);
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(),getAllPackageNames(arrayList[Productivity]), Productivity);
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(),getAllPackageNames(arrayList[Games]), Games);
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(),getAllPackageNames(arrayList[Media]), Media);
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(),getAllPackageNames(arrayList[Social]), Social);
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(),getAllPackageNames(arrayList[Lifestyle]), Lifestyle);

            //set labels
            SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            settingsPrefs.edit().putString("groupName"+Other, "Other").apply();
            settingsPrefs.edit().putString("groupName"+Reading, "Reading").apply();
            settingsPrefs.edit().putString("groupName"+Productivity, "Productivity").apply();
            settingsPrefs.edit().putString("groupName"+Games, "Games").apply();
            settingsPrefs.edit().putString("groupName"+Media, "Media").apply();
            settingsPrefs.edit().putString("groupName"+Social, "Social").apply();
            settingsPrefs.edit().putString("groupName"+Lifestyle, "Lifestyle").apply();

            //set icons
            SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("SettingsActivity",Context.MODE_PRIVATE).edit();
            editor.putLong("iconID"+Other, R.drawable.ic_settings_black_24dp).apply();
            editor.putLong("iconID"+Reading, R.drawable.ic_local_library_black_24dp).apply();
            editor.putLong("iconID"+Productivity, R.drawable.ic_insert_chart_black_24dp).apply();
            editor.putLong("iconID"+Games, R.drawable.ic_videogame_asset_black_24dp).apply();
            editor.putLong("iconID"+Media, R.drawable.play_circle_filled).apply();
            editor.putLong("iconID"+Social, R.drawable.ic_chat_black_24dp).apply();
            editor.putLong("iconID"+Lifestyle, R.drawable.ic_favorite_black_24dp).apply();

            //updates numGroups to 7
            settingsPrefs.edit().putString("numZones","7").apply();
            finish();
        }

        private ArrayList<String> getAllPackageNames(ArrayList<AppInfo> appInfo){
            ArrayList<String> labels = new ArrayList<>();
            for (AppInfo app:appInfo) {
                labels.add((String) app.packageName);
                Log.d("favorites", "Adding " + app.packageName);
            }
            return labels;
        }
    }
}
