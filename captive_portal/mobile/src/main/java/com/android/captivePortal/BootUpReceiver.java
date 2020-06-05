package com.android.captivePortal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.IOException;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences myPrefs;
            SharedPreferences.Editor myPrefsPrefsEditor;
            String myPref="onBoot";
            myPrefs=context.getSharedPreferences(myPref,Context.MODE_PRIVATE);
            boolean check=myPrefs.getBoolean("setBoot",false);
            if (check) {
                myPrefsPrefsEditor=myPrefs.edit();
                myPrefsPrefsEditor.putBoolean("enabled",true);
                myPrefsPrefsEditor.apply();
                try {
                    Runtime.getRuntime().exec("su");
                    Runtime.getRuntime().exec(context.getFilesDir().getAbsolutePath() + "/capstart");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
