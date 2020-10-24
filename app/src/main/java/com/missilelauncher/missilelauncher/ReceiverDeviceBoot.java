package com.missilelauncher.missilelauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ReceiverDeviceBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            FloatingWindow.enqueueWork(context, new Intent());
        }
    }

}
