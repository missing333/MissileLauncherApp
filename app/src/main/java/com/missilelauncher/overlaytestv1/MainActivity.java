package com.missilelauncher.overlaytestv1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.v4.app.ServiceCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.missilelauncher.overlaytestv1.G1SelectedItems.G1SelectedApps;
import static com.missilelauncher.overlaytestv1.G2SelectedItems.G2SelectedApps;
import static com.missilelauncher.overlaytestv1.G3SelectedItems.G3SelectedApps;
import static com.missilelauncher.overlaytestv1.G4SelectedItems.G4SelectedApps;
import static com.missilelauncher.overlaytestv1.G5SelectedItems.G5SelectedApps;
import static com.missilelauncher.overlaytestv1.G6SelectedItems.G6SelectedApps;
import static com.missilelauncher.overlaytestv1.G7SelectedItems.G7SelectedApps;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    private Button start, stop, config;
    private Switch enableToggle;
    public SharedPreferences.Editor prefEditor;
    private View B1, B2, B3, B4, B5, B6, B7;
    private ArrayList<AppInfo>[] groupAppList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("SettingsActivity", 0);
        prefEditor = sharedPref.edit();

        setContentView(R.layout.activity_main);

        config = findViewById(R.id.config);
        enableToggle = findViewById(R.id.enableService);

        enableToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    Intent startIntent = new Intent(getApplication(), FloatingWindow.class);
                    startIntent.setAction("Start");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.v("app","Starting Foreground Service");
                        Toast.makeText(getApplication(),"Starting as Foreground Service." ,Toast.LENGTH_SHORT ).show();
                        startForegroundService(startIntent);
                    } else {
                        Log.v("app","Starting regular Service");
                        Toast.makeText(getApplication(),"Starting as regular Service." ,Toast.LENGTH_SHORT ).show();
                        startService(startIntent);
                    }
                }
                else {
                    Log.d("app", "Stopping Service");
                    Intent stopIntent = new Intent(MainActivity.this, FloatingWindow.class);
                    stopIntent.setAction("Stop");
                    stopService(stopIntent);
                }
            }
        });

        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });


        ////////set all previous applists if available
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

        Collections.sort(res, AppInfo.appNameComparator);
        SharedListPreferencesHelper sh = new SharedListPreferencesHelper();

        groupAppList = new ArrayList[10];
        groupAppList[1] = G1SelectedApps;
        groupAppList[2] = G2SelectedApps;
        groupAppList[3] = G3SelectedApps;
        groupAppList[4] = G4SelectedApps;
        groupAppList[5] = G5SelectedApps;
        groupAppList[6] = G6SelectedApps;
        groupAppList[7] = G7SelectedApps;

        for (int g=1;g<7+1;g++){
            ArrayList saveList = sh.getFavorites(getApplicationContext(),g);
            if (saveList == null){
                saveList = new ArrayList<>(0);
            }
            groupAppList[g] = new ArrayList<AppInfo>(0);
            if(res.size() > 0 && saveList != null){
                for (int i=0;i<res.size();i++) {
                    //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                    for (int f=0;f<saveList.size();f++) {
                        //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                        if(res.get(i).packageName.equals(saveList.get(f))){
                            groupAppList[g].add(res.get(i));
                        }
                    }
                }
            }
        }
        new G1SelectedItems().setG1Apps(groupAppList[1]);
        new G2SelectedItems().setG2Apps(groupAppList[2]);
        new G3SelectedItems().setG3Apps(groupAppList[3]);
        new G4SelectedItems().setG4Apps(groupAppList[4]);
        new G5SelectedItems().setG5Apps(groupAppList[5]);
        new G6SelectedItems().setG6Apps(groupAppList[6]);
        new G7SelectedItems().setG7Apps(groupAppList[7]);

    }

    @Override
    protected void onStart() {
        super.onStart();

        start = (Button) findViewById(R.id.start);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            start.setVisibility(View.VISIBLE);
        } else {
            start.setVisibility(View.GONE);
            Log.v("ol", "Ready to draw overlays");

            Intent startIntent = new Intent(this, FloatingWindow.class);
            startIntent.setAction("Start");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.v("app","Starting Foreground Service");
                Toast.makeText(this,"Starting as Foreground Service." ,Toast.LENGTH_SHORT ).show();
                startForegroundService(startIntent);
            } else {
                Log.v("app","Starting regular Service");
                Toast.makeText(this,"Starting as regular Service." ,Toast.LENGTH_SHORT ).show();
                startService(startIntent);
            }
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        parent.getItemAtPosition(position);
        position++;
        parent.setOnItemSelectedListener(this);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
