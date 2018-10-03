package com.missilelauncher.overlaytestv1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;

public class AppInfo {
    CharSequence label;
    CharSequence packageName;
    Drawable icon;
    String versionName;
    Intent launchIntent = null;
    int versionCode;
    int x;
    int y;
    int launchCount;

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

    public void setLaunchCount(int launchCount) {
        this.launchCount = launchCount;
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

}
