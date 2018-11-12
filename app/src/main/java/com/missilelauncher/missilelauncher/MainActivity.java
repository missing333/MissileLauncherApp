package com.missilelauncher.missilelauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity {

    SharedPreferences settingsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button config = findViewById(R.id.config);
        final Switch enableToggle = findViewById(R.id.enableService);
        final Switch enableForegroundNotif = findViewById(R.id.foregroundNotification);
        settingsPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        if (settingsPrefs.getBoolean("foregroundNotif", true)){
            enableForegroundNotif.setChecked(true);
        }else{
            enableForegroundNotif.setChecked(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enableForegroundNotif.setVisibility(View.VISIBLE);
        }
        else{
            enableForegroundNotif.setVisibility(View.GONE);
        }

        enableToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                final SharedPreferences.Editor editor = settingsPrefs.edit();
                if (compoundButton.isChecked()) {
                    editor.putBoolean("appEnabled", true).commit();
                    if(Settings.canDrawOverlays(MainActivity.this)) {
                        Intent startIntent = new Intent(getApplication(), FloatingWindow.class);
                        startIntent.setAction("Start");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && enableForegroundNotif.isChecked()) {
                            Log.v("app", "Starting Foreground Service");
                            Toast.makeText(getApplication(), "Starting as Foreground Service.", Toast.LENGTH_SHORT).show();
                            startForegroundService(startIntent);
                        } else {
                            Log.v("app", "Starting regular Service");
                            Toast.makeText(getApplication(), "Starting as regular Service.", Toast.LENGTH_SHORT).show();
                            startService(startIntent);
                        }
                    }
                    else{
                        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivity(myIntent);
                    }
                    enableForegroundNotif.setEnabled(true);
                }
                else {
                    Log.d("app", "Stopping Service");
                    editor.putBoolean("appEnabled", false).commit();
                    try {
                        Intent stopIntent = new Intent(MainActivity.this, FloatingWindow.class);
                        stopIntent.setAction("Stop");
                        stopService(stopIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    enableForegroundNotif.setEnabled(false);
                }
            }
        });


        enableForegroundNotif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                final SharedPreferences.Editor editor = settingsPrefs.edit();

                Intent intent = new Intent(MainActivity.this, FloatingWindow.class);
                Log.d("app", "Stopping Service");
                try {
                    Intent stopIntent = new Intent(MainActivity.this, FloatingWindow.class);
                    stopIntent.setAction("Stop");
                    stopService(stopIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                intent.setAction("Start");
                if (compoundButton.isChecked()) {       //user wants notification
                    editor.putBoolean("foregroundNotif", true).commit();
                    if (enableToggle.isChecked()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.v("app", "Starting Foreground Service");
                            Toast.makeText(getApplication(), "Starting as Foreground Service.", Toast.LENGTH_SHORT).show();
                            startForegroundService(intent);
                        } else {
                            Log.v("app", "Starting regular Service");
                            Toast.makeText(getApplication(), "Starting as regular Service.", Toast.LENGTH_SHORT).show();
                            startService(intent);
                        }
                    }

                }
                else {                                  //user doesn't want notification
                    editor.putBoolean("foregroundNotif", false).commit();
                    if (enableToggle.isChecked()) {
                        Log.v("app", "Starting regular Service");
                        Toast.makeText(getApplication(), "Starting as regular Service.", Toast.LENGTH_SHORT).show();
                        startService(intent);
                    }
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

        init();
    }

    protected boolean isProInstalled(Context context) {
        PackageManager manager = context.getPackageManager();
        try {
            if (manager.checkSignatures(context.getPackageName(), "com.missing.missilelauncherpro")
                    == PackageManager.SIGNATURE_MATCH) {
                //Pro key installed, and signatures match
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    void init(){

        ////////set all previous applists if available
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

        Collections.sort(res, AppInfo.appNameComparator);
        SharedListPreferencesHelper sh = new SharedListPreferencesHelper();

        ArrayList[] groupAppList = new ArrayList[10];
        groupAppList[1] = G1SelectedItems.G1SelectedApps;
        groupAppList[2] = G2SelectedItems.G2SelectedApps;
        groupAppList[3] = G3SelectedItems.G3SelectedApps;
        groupAppList[4] = G4SelectedItems.G4SelectedApps;
        groupAppList[5] = G5SelectedItems.G5SelectedApps;
        groupAppList[6] = G6SelectedItems.G6SelectedApps;
        groupAppList[7] = G7SelectedItems.G7SelectedApps;

        for (int g=1;g<7+1;g++){
            ArrayList saveList = sh.getFavorites(getApplicationContext(),g);
            if (saveList == null){
                saveList = new ArrayList<>(0);
            }
            groupAppList[g] = new ArrayList<>(0);
            if(res.size() > 0 ){
                for (int i=0;i<res.size();i++) {
                    //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                    for (int f=0;f<saveList.size();f++) {
                        //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                        if(res.get(i).packageName.equals(saveList.get(f))){
                            //add the package name (ie: com.google.youtube) to groupAppList
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

        Button setPermissionBtn = findViewById(R.id.setPermissionBtn);
        ImageView imgHow = findViewById(R.id.imgHow);
        Switch enableNotif = findViewById(R.id.foregroundNotification);
        Button config = findViewById(R.id.config);
        final Switch enableToggle = findViewById(R.id.enableService);
        final Switch enableForegroundNotif = findViewById(R.id.foregroundNotification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            setPermissionBtn.setVisibility(View.VISIBLE);
            imgHow.setVisibility(View.VISIBLE);
            config.setVisibility(View.GONE);
            enableToggle.setVisibility(View.GONE);
            enableForegroundNotif.setVisibility(View.GONE);


            setPermissionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivity(myIntent);
                }
            });


        } else {
            setPermissionBtn.setVisibility(View.GONE);
            imgHow.setVisibility(View.GONE);
            config.setVisibility(View.VISIBLE);
            enableToggle.setVisibility(View.VISIBLE);
            enableForegroundNotif.setVisibility(View.VISIBLE);


            Log.v("ol", "Ready to draw overlays");

            Intent startIntent = new Intent(this, FloatingWindow.class);
            startIntent.setAction("Start");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && enableNotif.isChecked()) {
                Log.v("app","Starting Foreground Service");
                //Toast.makeText(this,"Starting as Foreground Service." ,Toast.LENGTH_SHORT ).show();
                startForegroundService(startIntent);
            } else {
                Log.v("app","Starting regular Service");
                //Toast.makeText(this,"Starting as regular Service." ,Toast.LENGTH_SHORT ).show();
                startService(startIntent);
            }
        }


    }

}
