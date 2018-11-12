package com.missilelauncher.missilelauncher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AutoSort extends AppCompatActivity {

    public final static String GOOGLE_URL = "https://play.google.com/store/apps/details?id=";
    public static final String ERROR = "error";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_sort);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .8), (int) (height * .5));

        new FetchCategoryTask().execute();
    }

    private class FetchCategoryTask extends AsyncTask<ArrayList<AppInfo>[], Void,ArrayList<AppInfo>[]> {

        private final String TAG = FetchCategoryTask.class.getSimpleName();
        private PackageManager pm;
        private int Other=1, Reading=2, Productivity=3, Games=4, Media=5, Social=6, Lifestyle=7;

        private String getCategory(String query_url) {
            boolean network = isNetworkAvailable();
            if (!network) {
                //manage connectivity lost
                Toast.makeText(getApplicationContext(), "Network Connectivity lost.  Please try again with a more stable network.", Toast.LENGTH_LONG).show();
                return ERROR;
            } else {
                try {
                    Document doc = Jsoup.connect(query_url)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                            .referrer("http://www.google.com")
                            .timeout(1000*5)
                            .get();
                    //Log.d("autosort",doc.text());
                    Element link = doc.select("a[itemprop=genre]").first();
                    return link.text();
                } catch (Exception e) {
                    //e.printStackTrace();
                    return ERROR;
                }
            }
        }

        // Check all connectivities whether available or not
        public boolean isNetworkAvailable() {
            ConnectivityManager cm = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
            return false;
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

            while (iterator.hasNext()) {
                ApplicationInfo packageInfo = iterator.next();
                String query_url = GOOGLE_URL + packageInfo.packageName;
                if (getPackageManager().getLaunchIntentForPackage(packageInfo.packageName) != null){
                    category = getCategory(query_url);

                    Log.d("autosort",packageInfo.packageName + ": " + category);
                    //Log.i(TAG, query_url);

                    AppInfo newInfo = new AppInfo();
                    newInfo.icon = packageInfo.loadIcon(getPackageManager());
                    newInfo.setPackageName(packageInfo.packageName);
                    newInfo.setLabel(packageInfo.loadLabel(getPackageManager()).toString());
                    newInfo.setLaunchIntent(getPackageManager().getLaunchIntentForPackage(packageInfo.packageName));

                    // store category or do something else
                    switch (category){
                        case "Tools":
                            groupAppList[Other].add(newInfo);
                            break;
                        case "Arcade":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Puzzle":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Cards":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Casual":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Racing":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Sport":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Action":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Adventure":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Board":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Casino":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Educational":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Music":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Role Playing":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Simulation":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Strategy":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Trivia":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Word Games":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Entertainment":
                            groupAppList[Media].add(newInfo);
                            break;
                        case "Games":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "News & Magazines":
                            groupAppList[Reading].add(newInfo);
                            break;
                        case "Comics":
                            groupAppList[Reading].add(newInfo);
                            break;
                        case "Books & Reference":
                            groupAppList[Reading].add(newInfo);
                            break;
                        case "Finance":
                            groupAppList[Productivity].add(newInfo);
                            break;
                        case "Productivity":
                            groupAppList[Productivity].add(newInfo);
                            break;
                        case "Business":
                            groupAppList[Productivity].add(newInfo);
                            break;
                        case "Social":
                            groupAppList[Social].add(newInfo);
                            break;
                        case "Dating":
                            groupAppList[Social].add(newInfo);
                            break;
                        case "Communication":
                            groupAppList[Social].add(newInfo);
                            break;
                        case "Photography":
                            groupAppList[Media].add(newInfo);
                            break;
                        case "Video Players & Editors":
                            groupAppList[Media].add(newInfo);
                            break;
                        case "Music & Audio":
                            groupAppList[Media].add(newInfo);
                            break;
                        case "Health & Fitness":
                            groupAppList[Lifestyle].add(newInfo);
                            break;
                        case "Sports":
                            groupAppList[Games].add(newInfo);
                            break;
                        case "Shopping":
                            groupAppList[Lifestyle].add(newInfo);
                            break;
                        case "Weather":
                            groupAppList[Lifestyle].add(newInfo);
                            break;
                        case "Lifestyle":
                            groupAppList[Lifestyle].add(newInfo);
                            break;
                        case "Travel & Local":
                            groupAppList[Lifestyle].add(newInfo);
                            break;
                        case "House & Home":
                            groupAppList[Lifestyle].add(newInfo);
                            break;
                        case "Food & Drink":
                            groupAppList[Lifestyle].add(newInfo);
                            break;
                        case "Maps & Navigation":
                            groupAppList[Lifestyle].add(newInfo);
                            break;


                        default:
                            groupAppList[Other].add(newInfo);
                            break;
                    }
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
