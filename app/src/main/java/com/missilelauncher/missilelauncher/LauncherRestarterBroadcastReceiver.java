package com.missilelauncher.missilelauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class LauncherRestarterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LauncherRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                context.startService(new Intent(context, FloatingWindow.class));
            }
        }, 5000);
    }
}
