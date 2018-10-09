package com.missilelauncher.overlaytestv1;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.missilelauncher.overlaytestv1.G1SelectedItems.G1SelectedApps;
import static com.missilelauncher.overlaytestv1.G2SelectedItems.G2SelectedApps;
import static com.missilelauncher.overlaytestv1.G3SelectedItems.G3SelectedApps;
import static com.missilelauncher.overlaytestv1.G4SelectedItems.G4SelectedApps;
import static com.missilelauncher.overlaytestv1.G5SelectedItems.G5SelectedApps;
import static com.missilelauncher.overlaytestv1.G6SelectedItems.G6SelectedApps;
import static com.missilelauncher.overlaytestv1.G7SelectedItems.G7SelectedApps;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    private Button b;
    public SharedPreferences.Editor prefEditor;
    private View B1, B2, B3, B4, B5, B6, B7;
    private ArrayList<AppInfo>[] groupAppList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("SettingsActivity", 0);
        prefEditor = sharedPref.edit();

        setContentView(R.layout.activity_main);

        prefEditor.putInt("transparency",55 );

        String[] groupSizes = {"1", "2", "3", "4", "5", "6", "7"};
        String[] sizes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14"};
        //int[] sizes = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        Spinner numGroupSpinner = (Spinner) findViewById(R.id.groupSpinner);
        Spinner numAppColsSpinner = (Spinner) findViewById(R.id.appColSpinner);
        Spinner numAppRowsSpinner = (Spinner) findViewById(R.id.appRowSpinner);

        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this,R.layout.list_text_view, groupSizes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numGroupSpinner.setAdapter(aa);

        int spinnerPosition;
        numGroupSpinner.setAdapter(aa);
        spinnerPosition = aa.getPosition(sharedPref.getInt("numGroups",7)+"");
        numGroupSpinner.setSelection(spinnerPosition);
        numGroupSpinner.setOnItemSelectedListener(this);
        aa = new ArrayAdapter(this,R.layout.list_text_view, sizes);
        numAppColsSpinner.setAdapter(aa);
        spinnerPosition = aa.getPosition(sharedPref.getInt("numAppCols",9)+"");
        numAppColsSpinner.setSelection(spinnerPosition);
        numAppColsSpinner.setOnItemSelectedListener(this);
        numAppRowsSpinner.setAdapter(aa);
        spinnerPosition = aa.getPosition(sharedPref.getInt("numAppRows",11)+"");
        numAppRowsSpinner.setSelection(spinnerPosition);
        numAppRowsSpinner.setOnItemSelectedListener(this);


        ///////////////////start groups section//////////////////////////////////////


        B1 = findViewById(R.id.G1);
        //set listener for Button event
        B1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G1SelectedItems.class);
                startActivity(intent);
            }
        });

        B2 = findViewById(R.id.G2);
        //set listener for Button event
        B2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G2SelectedItems.class);
                startActivity(intent);
            }
        });

        B2 = findViewById(R.id.G2);
        //set listener for Button event
        B2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G2SelectedItems.class);
                startActivity(intent);
            }
        });

        B3 = findViewById(R.id.G3);
        //set listener for Button event
        B3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G3SelectedItems.class);
                startActivity(intent);
            }
        });

        B4 = findViewById(R.id.G4);
        //set listener for Button event
        B4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G4SelectedItems.class);
                startActivity(intent);
            }
        });

        B5 = findViewById(R.id.G5);
        //set listener for Button event
        B5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G5SelectedItems.class);
                startActivity(intent);
            }
        });

        B6 = findViewById(R.id.G6);
        //set listener for Button event
        B6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G6SelectedItems.class);
                startActivity(intent);
            }
        });

        B7 = findViewById(R.id.G7);
        //set listener for Button event
        B7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, G7SelectedItems.class);
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

        b = (Button) findViewById(R.id.start);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            b.setVisibility(View.VISIBLE);
        } else {
            b.setVisibility(View.GONE);
            Log.v("ol", "Ready to draw overlays");
            G1SelectedItems g1 = new G1SelectedItems();
            startService(new Intent(MainActivity.this, FloatingWindow.class));
        }

        b.setOnClickListener(new View.OnClickListener() {
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
        if (parent.toString().contains("groupSpinner")){
            prefEditor.putInt("numGroups",position).commit();
            Log.v("prefs","numGroups = " + position + " now.");
        }
        else if(parent.toString().contains("appRowSpinner")){
            prefEditor.putInt("numAppRows",position).commit();
            Log.v("prefs","numAppRows = " + position + " now.");
        }
        else if(parent.toString().contains("appColSpinner")){
            prefEditor.putInt("numAppCols",position).commit();
            Log.v("prefs","numAppCols = " + position + " now.");
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        prefEditor.putInt("transparency",0 );
        FloatingWindow fw = new FloatingWindow();
        try {
            fw.setTransparency(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
