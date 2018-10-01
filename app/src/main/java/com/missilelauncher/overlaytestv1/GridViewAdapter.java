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
    public List selectedPositions;

    public GridViewAdapter(AppInfo[] appList, Activity activity) {
        this.appArray = appList;
        this.activity = activity;
        selectedPositions = new ArrayList<>();
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
        customView.display(appArray[position].label.toString(), appArray[position].icon, selectedPositions.contains(position));
        return customView;
    }
}
