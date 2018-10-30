package com.missilelauncher.missilelauncher;

/**
 * Created by mmissildine on 1/24/2018.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {



            Intent startIntent = new Intent(context, FloatingWindow.class);
            startIntent.setAction("Start");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.v("app","Starting Foreground Service");
                //Toast.makeText(this,"Starting as Foreground Service." ,Toast.LENGTH_SHORT ).show();
                context.startForegroundService(startIntent);
            } else {
                Log.v("app","Starting regular Service");
                //Toast.makeText(this,"Starting as regular Service." ,Toast.LENGTH_SHORT ).show();
                context.startService(startIntent);
            }
            Log.d("app", "Started app from BootReceiver.");
        }
    }
}
