package com.missilelauncher.overlaytestv1;

/**
 * Created by mmissildine on 9/28/2018.
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class G3SelectedItems extends AppCompatActivity {

    private GridView gridView;
    public static ArrayList<AppInfo> G3SelectedApps;
    public AppInfo[] appArray;
    SharedListPreferencesHelper sh = new SharedListPreferencesHelper();
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    public static ArrayList<String> saveList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int group = 3;

        sharedPrefs = getSharedPreferences("SettingsActivity",0 );
        editor = sharedPrefs.edit();

        setContentView(R.layout.group_picking);

        final EditText gName = (EditText) findViewById(R.id.editName);
        gName.setText(sharedPrefs.getString("G3 Name","" ));
        gName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count){
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after){
            }

            @Override
            public void afterTextChanged(Editable s){
                editor.putString("G3 Name", s.toString()).commit();
            }
        });

        ImageButton gIcon = (ImageButton) findViewById(R.id.gIcon);
        gIcon.setVisibility(View.GONE);


        appArray = getPackages().toArray(new AppInfo[0]);
        Collections.sort(Arrays.asList(appArray), AppInfo.appNameComparator);

        saveList = sh.getFavorites(getApplicationContext(),group);
        if (saveList == null){
            saveList = new ArrayList<>(0);
        }

        Button b = findViewById(R.id.saveButton);
        gridView = (GridView) findViewById(R.id.gridView);


        final GridViewAdapter adapter = new GridViewAdapter(appArray, this);
        gridView.setAdapter(adapter);
        GridItemView gv = new GridItemView(getApplicationContext());
        adapter.groupIndex = group;

        G3SelectedApps = new ArrayList<AppInfo>(0);
        if(appArray.length > 0 && saveList != null){
            for (int i=0;i<appArray.length;i++) {
                //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                gv.display( appArray[i].label.toString(),appArray[i].icon, false);
                for (int f=0;f<saveList.size();f++) {
                    //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                    if(appArray[i].packageName.equals(saveList.get(f))){
                        adapter.listOfLists[group].add(i);
                        G3SelectedApps.add(appArray[i]);
                        adapter.getView(i, gv ,gridView );
                        gv.display( appArray[i].label.toString(),appArray[i].icon, true);
                    }
                }
            }
        }



        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int selectedIndex = adapter.listOfLists[group].indexOf(position);
                if (selectedIndex > -1) {
                    adapter.listOfLists[group].remove(selectedIndex);
                    ((GridItemView) v).display(false);
                    G3SelectedApps.remove((AppInfo) parent.getItemAtPosition(position));
                    saveList.remove((String) ((AppInfo) parent.getItemAtPosition(position)).packageName);
                } else {
                    adapter.listOfLists[group].add(position);
                    ((GridItemView) v).display(true);
                    G3SelectedApps.add((AppInfo) parent.getItemAtPosition(position));
                    saveList.add((String) ((AppInfo) parent.getItemAtPosition(position)).packageName);
                }
            }
        });


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sh.saveFavorites(getApplicationContext(), saveList ,group );
                editor.putString("Name of G3", gName.getText().toString()).commit();

                for (int i = 0; i< G3SelectedApps.size(); i++){
                    Log.v("G3 apps","App " + i +": " + G3SelectedApps.get(i).label);
                }
                Toast.makeText(G3SelectedItems.this,"Apps Saved!",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        gIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private ArrayList<AppInfo> getPackages() {
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
}
