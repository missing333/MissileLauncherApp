package com.missilelauncher.missilelauncher;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@SuppressWarnings({"SpellCheckingInspection", "unchecked"})
public class MainActivity extends AppCompatActivity {

    private Intent mServiceIntent;
    private Context ctx;
    private Context getCtx() {
        return ctx;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;

        setContentView(R.layout.activity_main);

        FloatingWindow mFloatingWindowService = new FloatingWindow(getCtx());
        mServiceIntent = new Intent(getCtx(), mFloatingWindowService.getClass());


        Button config = findViewById(R.id.config);

        if(Settings.canDrawOverlays(MainActivity.this)) {
            if (!isMyServiceRunning(mFloatingWindowService.getClass())) {
                startService(mServiceIntent);
            }
        }


        config.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        init();
    }

    private void init(){

        ////////set all previous app lists if available
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
        Button config = findViewById(R.id.config);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            setPermissionBtn.setVisibility(View.VISIBLE);
            imgHow.setVisibility(View.VISIBLE);
            config.setVisibility(View.GONE);


            setPermissionBtn.setOnClickListener(v -> {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivity(myIntent);
            });


        } else {
            setPermissionBtn.setVisibility(View.GONE);
            imgHow.setVisibility(View.GONE);
            config.setVisibility(View.VISIBLE);


            Log.v("ol", "Ready to draw overlays");

            Intent startIntent = new Intent(this, FloatingWindow.class);
            startIntent.setAction("Start");
            startService(startIntent);

        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

}
