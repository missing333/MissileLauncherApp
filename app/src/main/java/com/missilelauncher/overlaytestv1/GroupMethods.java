package com.missilelauncher.overlaytestv1;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.GridView;
import java.util.ArrayList;
import java.util.Iterator;
import static com.missilelauncher.overlaytestv1.G1SelectedItems.G1SelectedApps;
import static com.missilelauncher.overlaytestv1.G2SelectedItems.G2SelectedApps;
import static com.missilelauncher.overlaytestv1.G3SelectedItems.G3SelectedApps;
import static com.missilelauncher.overlaytestv1.G4SelectedItems.G4SelectedApps;
import static com.missilelauncher.overlaytestv1.G5SelectedItems.G5SelectedApps;
import static com.missilelauncher.overlaytestv1.G6SelectedItems.G6SelectedApps;
import static com.missilelauncher.overlaytestv1.G7SelectedItems.G7SelectedApps;

public class GroupMethods extends AppCompatActivity {
    SharedListPreferencesHelper sh = new SharedListPreferencesHelper();
    GridViewAdapter adapter;
    public static ArrayList<String> saveList;
    private GridView gridView;


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

    void displayGrid(AppInfo[] appArray, Integer group){
        gridView = (GridView) findViewById(R.id.gridView);
        adapter = new GridViewAdapter(appArray, this);
        gridView.setAdapter(adapter);
        GridItemView gv = new GridItemView(getApplicationContext());
        adapter.groupIndex = group;

        saveList = sh.getFavorites(this,group);
        if (saveList == null){
            saveList = new ArrayList<>(0);
        }
        if(appArray.length > 0 && saveList != null){
            for (int i=0;i<appArray.length;i++) {
                //Log.v("Setting","appArray["+ i + "] " + appArray[i].packageName);
                gv.display( appArray[i].label.toString(),appArray[i].icon, false);
                for (int f=0;f<saveList.size();f++) {
                    //Log.v("Setting","saveList "+ f + ": " + saveList.get(f));
                    if(appArray[i].packageName.equals(saveList.get(f))){
                        adapter.listOfLists[group].add(i);
                        switch (group){
                            case 1:
                                G1SelectedApps.add(appArray[i]);
                                break;
                            case 2:
                                G2SelectedApps.add(appArray[i]);
                                break;
                            case 3:
                                G3SelectedApps.add(appArray[i]);
                                break;
                            case 4:
                                G4SelectedApps.add(appArray[i]);
                                break;
                            case 5:
                                G5SelectedApps.add(appArray[i]);
                                break;
                            case 6:
                                G6SelectedApps.add(appArray[i]);
                                break;
                            case 7:
                                G7SelectedApps.add(appArray[i]);
                                break;
                        }
                        adapter.getView(i, gv ,gridView );
                        gv.display( appArray[i].label.toString(),appArray[i].icon, true);
                    }
                }
            }
        }
    }


}
