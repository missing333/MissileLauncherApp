package com.missilelauncher.missilelauncher;

/**
 * Created by mmissildine on 9/28/2018.
 */

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class G1SelectedItems extends AppCompatActivity {

    private GridView gridView;
    public static ArrayList<AppInfo> G1SelectedApps;
    public AppInfo[] appArray;
    SharedListPreferencesHelper sh = new SharedListPreferencesHelper();
    public static ArrayList<String> saveList;
    final int group = 1;
    GridViewAdapter adapter;
    public AppInfo[] uncategorizedAppArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.group_picking);

        appArray = getPackages().toArray(new AppInfo[0]);
        Arrays.sort(appArray, AppInfo.appNameComparator);
        uncategorizedAppArray = appArray;

        saveList = sh.getFavorites(getApplicationContext(),group);
        if (saveList == null){
            saveList = new ArrayList<>(0);
        }

        Button b = findViewById(R.id.saveButton);
        gridView = findViewById(R.id.gridView);


        Switch uca = findViewById(R.id.uncategoriezedButton);
        if (uca.isChecked()){
            ArrayList<AppInfo> allUncategoriezedApps = new ArrayList<AppInfo>(Arrays.asList(appArray));
            allUncategoriezedApps = getUncategoriezedApps(allUncategoriezedApps,group );
            uncategorizedAppArray = allUncategoriezedApps.toArray(new AppInfo[allUncategoriezedApps.size()]);
            displayGrid(uncategorizedAppArray);
        }else{
            displayGrid(appArray);
        }

        uca.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (compoundButton.isChecked()) {
                    ArrayList<AppInfo> allUncategoriezedApps = new ArrayList<AppInfo>(Arrays.asList(appArray));
                    allUncategoriezedApps = getUncategoriezedApps(allUncategoriezedApps,group );
                    uncategorizedAppArray = allUncategoriezedApps.toArray(new AppInfo[allUncategoriezedApps.size()]);
                    displayGrid(uncategorizedAppArray);

                } else {
                    displayGrid(appArray);
                }


            }
        });






        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
            }
        });


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedListPreferencesHelper.saveFavorites(getApplicationContext(), saveList ,group );

                for (int i = 0; i< G1SelectedApps.size(); i++){
                    Log.v("g1 apps","App " + i +": " + G1SelectedApps.get(i).label);
                }
                Toast.makeText(G1SelectedItems.this,"Apps Saved!",Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }


    public ArrayList<AppInfo> getPackages() {
        ArrayList<AppInfo> apps = getInstalledApps(false); /* false = no system packages */
        final int max = apps.size();
        for (int i=0; i<max; i++) {
            apps.get(i).prettyPrint();
        }
        return apps;
    }

    private ArrayList<AppInfo> getInstalledApps(boolean getSysPackages) {
        ArrayList<AppInfo> res = new ArrayList<AppInfo>();
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

    public ArrayList<AppInfo> getUncategoriezedApps (ArrayList<AppInfo> allApps, Integer group){
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

    void displayGrid(AppInfo[] appArray){
        adapter = new GridViewAdapter(appArray, this);
        gridView.setAdapter(adapter);
        GridItemView gv = new GridItemView(getApplicationContext());
        adapter.groupIndex = group;

        saveList = sh.getFavorites(getApplicationContext(),group);
        if (saveList == null){
            saveList = new ArrayList<>(0);
        }
        G1SelectedApps = new ArrayList<AppInfo>(0);
        if(appArray.length > 0 && saveList != null){
            for (int i=0;i<appArray.length;i++) {
                //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                gv.display( appArray[i].label.toString(),appArray[i].icon, false);
                for (int f=0;f<saveList.size();f++) {
                    //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                    if(appArray[i].packageName.equals(saveList.get(f))){
                        adapter.listOfLists[group].add(i);
                        G1SelectedApps.add(appArray[i]);
                        adapter.getView(i, gv ,gridView );
                        gv.display( appArray[i].label.toString(),appArray[i].icon, true);
                    }
                }
            }
        }
    }
}
