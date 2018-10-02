package com.missilelauncher.overlaytestv1;

/**
 * Created by mmissildine on 9/28/2018.
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class G2SelectedItems extends AppCompatActivity {

    private TextView textView;
    private GridView gridView;
    public static ArrayList<AppInfo> G2SelectedApps;
    public AppInfo[] appArray;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int group = 2;

        appArray = getPackages().toArray(new AppInfo[0]);

        setContentView(R.layout.group_picking);
        Button g2b = findViewById(R.id.saveButton);
        gridView = (GridView) findViewById(R.id.gridView);

        G2SelectedApps = new ArrayList<AppInfo>();

        final GridViewAdapter adapter = new GridViewAdapter(appArray, this);
        gridView.setAdapter(adapter);
        adapter.groupIndex = group;

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int selectedIndex = adapter.listOfLists[group].indexOf(position);
                if (selectedIndex > -1) {
                    adapter.listOfLists[group].remove(selectedIndex);
                    ((GridItemView) v).display(false);
                    G2SelectedApps.remove((AppInfo) parent.getItemAtPosition(position));
                } else {
                    adapter.listOfLists[group].add(position);
                    ((GridItemView) v).display(true);
                    G2SelectedApps.add((AppInfo) parent.getItemAtPosition(position));
                }
            }
        });


        g2b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences("SettingsActivity", 0);
                SharedPreferences.Editor prefEditor = sharedPref.edit();

                for (int i = 0; i< G2SelectedApps.size(); i++){
                    Log.v("g2 apps","App " + i +": " + G2SelectedApps.get(i).label);
                }
                Toast.makeText(G2SelectedItems.this,"Apps Saved!",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(G2SelectedItems.this, MainActivity.class);
                startActivity(intent);
            }
        });


        /*
        textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setPadding(10, 10, 10, 10);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setText("You selected: ");
        setContentView(textView);
        */


        //this returned the list to the other activity.
        //intent.putStringArrayListExtra("SELECTED_LETTER", G7SelectedApps);


        //This method gets the data from intent sent from another class/activity
        //getIntentData();
    }

/*    @SuppressLint("SetTextI18n")
    public void getIntentData() {
        ArrayList<String> stringArrayList = getIntent().getStringArrayListExtra("SELECTED_LETTER");

        assert stringArrayList != null;
        if (stringArrayList.size() > 0) {
            for (int i = 0; i < stringArrayList.size(); i++) {
                if (i < stringArrayList.size() - 1) {
                    textView.setText(textView.getText() + stringArrayList.get(i) + ", ");
                } else {
                    textView.setText(textView.getText() + stringArrayList.get(i) +".");
                }
            }
        }
    }*/


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
