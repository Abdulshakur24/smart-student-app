package com.ashakur.authfirebasse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            try {
                Class.forName("com.ashakur.authfirebasse.NotificationService");
                Intent serviceIntent = new Intent(context, NotificationService.class);
                context.startService(serviceIntent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                // NotificationService class doesn't exist, handle accordingly
            }
        }
    }
}