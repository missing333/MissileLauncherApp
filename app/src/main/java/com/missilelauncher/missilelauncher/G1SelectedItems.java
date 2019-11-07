package com.missilelauncher.missilelauncher;

/*
  Created by mmissildine on 9/28/2018.
 */

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"unchecked", "ToArrayCallWithZeroLengthArrayArgument"})
public class G1SelectedItems extends AppCompatActivity {

    private GridView gridView;
    public static ArrayList<AppInfo> G1SelectedApps;
    private AppInfo[] appArray;
    private final SharedListPreferencesHelper sh = new SharedListPreferencesHelper();
    private static ArrayList<String> saveList;
    private final int group = 1;
    private GridViewAdapter adapter;
    private AppInfo[] uncategorizedAppArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.group_picking);

        SharedPreferences settingsPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        getSupportActionBar().setTitle(settingsPrefs.getString("groupName"+group, "Group "+group));

        appArray = getPackages().toArray(new AppInfo[0]);
        Arrays.sort(appArray, AppInfo.appNameComparator);
        uncategorizedAppArray = appArray;

        saveList = sh.getFavorites(getApplicationContext(),group);
        if (saveList == null){
            saveList = new ArrayList<>(0);
        }



        Button b = findViewById(R.id.saveButton);
        gridView = findViewById(R.id.gridView);

        b.setOnClickListener(v -> {
            SharedListPreferencesHelper.saveFavorites(getApplicationContext(), saveList ,group );

            for (int i = 0; i< G1SelectedApps.size(); i++){
                Log.v("g1 apps","App " + i +": " + G1SelectedApps.get(i).label);
            }
            Toast.makeText(G1SelectedItems.this,"Apps Saved!",Toast.LENGTH_SHORT).show();
            finish();
        });


        Switch uca = findViewById(R.id.uncategoriezedButton);
        if (uca.isChecked()){
            ArrayList<AppInfo> allUncategorizedApps = new ArrayList<>(Arrays.asList(appArray));
            allUncategorizedApps = getUncategoriezedApps(allUncategorizedApps,group );
            uncategorizedAppArray = allUncategorizedApps.toArray(new AppInfo[allUncategorizedApps.size()]);
            displayGrid(uncategorizedAppArray);
        }else{
            displayGrid(appArray);
        }

        uca.setOnCheckedChangeListener((compoundButton, b1) -> {

            if (compoundButton.isChecked()) {
                ArrayList<AppInfo> allUncategorizedApps = new ArrayList<>(Arrays.asList(appArray));
                allUncategorizedApps = getUncategoriezedApps(allUncategorizedApps,group );
                uncategorizedAppArray = allUncategorizedApps.toArray(new AppInfo[allUncategorizedApps.size()]);
                displayGrid(uncategorizedAppArray);

            } else {
                displayGrid(appArray);
            }


        });






        gridView.setOnItemClickListener((parent, v, position, id) -> {
            int selectedIndex = adapter.listOfLists[group].indexOf(position);
            if (selectedIndex > -1) {
                adapter.listOfLists[group].remove(selectedIndex);
                ((GridItemView) v).display(false);
                G1SelectedApps.remove(parent.getItemAtPosition(position));
                saveList.remove(((AppInfo) parent.getItemAtPosition(position)).packageName);
            } else {
                adapter.listOfLists[group].add(position);
                ((GridItemView) v).display(true);
                G1SelectedApps.add((AppInfo) parent.getItemAtPosition(position));
                saveList.add((String) ((AppInfo) parent.getItemAtPosition(position)).packageName);
            }
        });





    }

    private ArrayList<AppInfo> getPackages() {
        ArrayList<AppInfo> apps = getInstalledApps(); /* false = no system packages */
        final int max = apps.size();
        for (int i=0; i<max; i++) {
            apps.get(i).prettyPrint();
        }
        return apps;
    }

    private ArrayList<AppInfo> getInstalledApps() {
        ArrayList<AppInfo> res = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packs = packageManager.getInstalledPackages(0);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            try{
                if(packageManager.getLaunchIntentForPackage(p.packageName) != null){

                    AppInfo newInfo = new AppInfo();
                    newInfo.setLabel(p.applicationInfo.loadLabel(getPackageManager()).toString());
                    newInfo.setPackageName(p.packageName);
                    newInfo.versionName = p.versionName;
                    newInfo.versionCode = p.versionCode;
                    newInfo.icon = p.applicationInfo.loadIcon(getPackageManager());
                    newInfo.setLaunchIntent(getPackageManager().getLaunchIntentForPackage(p.packageName));
                    res.add(newInfo);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.v("group","Number of Apps Found: " + res.size() );
        return res;
    }

    public void setG1Apps(ArrayList<AppInfo> app){
        G1SelectedApps = app;
    }

    private ArrayList<AppInfo> getUncategoriezedApps(ArrayList<AppInfo> allApps, @SuppressWarnings("SameParameterValue") Integer group){
        Log.v("uncategorizing", allApps.size() + " apps to start");
        ArrayList<String> saveList;
        Iterator<AppInfo> iterator;
        AppInfo a;
        for (int g=1;g<=7;g++){

            if (g != group){
                iterator = allApps.iterator();
                saveList = sh.getFavorites(getApplicationContext(),g);
                if(allApps.size() > 0 && saveList != null){
                    while (iterator.hasNext()) {
                        a = iterator.next();
                        for (int f=0;f<saveList.size();f++) {
                            if(a.packageName.equals(saveList.get(f))){
                                Log.v("uncategorizing", "Removing " + a.label + " from G" + group);
                                iterator.remove();
                            }
                        }
                    }
                }


            }
        }
        Log.v("uncategorizing", allApps.size() + " apps to end");
        return allApps;
    }

    private void displayGrid(AppInfo[] appArray){
        adapter = new GridViewAdapter(appArray, this);
        gridView.setAdapter(adapter);
        GridItemView gv = new GridItemView(getApplicationContext());
        adapter.groupIndex = group;

        saveList = sh.getFavorites(getApplicationContext(),group);
        if (saveList == null){
            saveList = new ArrayList<>(0);
        }
        G1SelectedApps = new ArrayList<>(0);
        if(appArray.length > 0 && saveList != null){
            for (int i=0;i<appArray.length;i++) {
                //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                gv.display(appArray[i].label,appArray[i].icon, false);
                for (int f=0;f<saveList.size();f++) {
                    //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                    if(appArray[i].packageName.equals(saveList.get(f))){
                        adapter.listOfLists[group].add(i);
                        G1SelectedApps.add(appArray[i]);
                        adapter.getView(i, gv ,gridView );
                        gv.display(appArray[i].label,appArray[i].icon, true);
                    }
                }
            }
        }
    }

}
