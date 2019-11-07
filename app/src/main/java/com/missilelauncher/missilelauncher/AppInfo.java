package com.missilelauncher.missilelauncher;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.util.Comparator;

public class AppInfo {
    String label = "";
    CharSequence packageName;
    Drawable icon;
    String versionName;
    Intent launchIntent = null;
    int versionCode;
    private int x;
    private int y;
    private int launchCount = 0;

    public AppInfo() {

    }

    public AppInfo(String label, int x, int y) {
        super();
        this.label = label;
        this.x = x;
        this.y = y;
        this.launchIntent = null;
    }

    public void setLabel(String label) {
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

    void setLaunchCount(int c) {
        this.launchCount = c;
    }

    public void setLaunchIntent(Intent launchIntent) {
        this.launchIntent = launchIntent;
    }

    String prettyPrint() {
        //Log.v("group",label + "\t\t\t" + packageName + "\t\t" + versionName + "\t" + versionCode + "\t\t" + launchIntent);
        return "appName: " + label + ", " + "launchIntent: " + launchIntent;
    }

    static final Comparator<AppInfo> appNameComparator;

    static {
        appNameComparator = (a1, a2) -> {
            String appName1 = a1.label + "";
            String appName2 = a2.label + "";

            //ascending order
            return appName1.compareTo(appName2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        };
    }

    /*Comparator for sorting the list by roll no*/
    public static Comparator<AppInfo> appLaunchCount = new Comparator<AppInfo>() {

        public int compare(AppInfo a1, AppInfo a2) {

            int launchCount1 = a1.launchCount;
            int launchCount2 = a2.launchCount;

            /*For ascending order*/
            //return rollno1-rollno2;

            /*For descending order*/
            //rollno2-rollno1;
            return launchCount2 - launchCount1;
        }
    };

}
