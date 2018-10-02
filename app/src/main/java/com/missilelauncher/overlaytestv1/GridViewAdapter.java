package com.missilelauncher.overlaytestv1;

/**
 * Created by mmissildine on 9/28/2018.
 */

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private Activity activity;
    private AppInfo[] appArray;
    public ArrayList<List> selectedPositions;
    public List G1list, G2list, G3list, G4list, G5list, G6list, G7list;
    public List[] listOfLists;
    public int groupIndex = 0;

    public GridViewAdapter(AppInfo[] appList, Activity activity) {
        this.appArray = appList;
        this.activity = activity;

        G1list = new ArrayList<>();
        G2list = new ArrayList<>();
        G3list = new ArrayList<>();
        G4list = new ArrayList<>();
        G5list = new ArrayList<>();
        G6list = new ArrayList<>();
        G7list = new ArrayList<>();
        listOfLists = new List[10];
        listOfLists[1] = G1list;
        listOfLists[2] = G2list;
        listOfLists[3] = G3list;
        listOfLists[4] = G4list;
        listOfLists[5] = G5list;
        listOfLists[6] = G6list;
        listOfLists[7] = G7list;
    }

    @Override
    public int getCount() {
        return appArray.length;
    }

    @Override
    public Object getItem(int position) {
        return appArray[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridItemView customView = (convertView == null) ? new GridItemView(activity) : (GridItemView) convertView;
        customView.display(appArray[position].label.toString(), appArray[position].icon, listOfLists[groupIndex].contains(position));
        return customView;
    }
}
