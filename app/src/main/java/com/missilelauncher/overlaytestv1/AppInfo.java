package com.missilelauncher.overlaytestv1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;

import java.util.Comparator;

public class AppInfo {
    CharSequence label;
    CharSequence packageName;
    Drawable icon;
    String versionName;
    Intent launchIntent = null;
    int versionCode;
    int x;
    int y;
    int launchCount = 0;

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public void setPackageName(CharSequence packageName) {
        this.packageName = packageName;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setLaunchCount(int c) {
        this.launchCount = c;
    }

    public void setLaunchIntent(Intent launchIntent) {
        this.launchIntent = launchIntent;
    }

    public static Drawable getActivityIcon(Context context, String packageName, String activityName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);

        return resolveInfo.loadIcon(pm);
    }

    public String prettyPrint() {
        //Log.v("group",label + "\t\t\t" + packageName + "\t\t" + versionName + "\t" + versionCode + "\t\t" + launchIntent);
        String r = "appName: " + label + ", " + "launchIntent: " + launchIntent;
        return r;
    }

    static Comparator<AppInfo> appNameComparator = new Comparator<AppInfo>() {

        public int compare(AppInfo a1, AppInfo a2) {
            String appName1 = a1.label+"";
            String appName2 = a2.label+"";

            //ascending order
            return appName1.compareTo(appName2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }};

    /*Comparator for sorting the list by roll no*/
    public static Comparator<AppInfo> appLaunchCount = new Comparator<AppInfo>() {

        public int compare(AppInfo a1, AppInfo a2) {

            int launchCount1 = a1.launchCount;
            int launchCount2 = a2.launchCount;

            /*For ascending order*/
            //return rollno1-rollno2;

            /*For descending order*/
            //rollno2-rollno1;
            return launchCount2-launchCount1;
        }};

}
