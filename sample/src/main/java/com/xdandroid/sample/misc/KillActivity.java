package com.xdandroid.sample.misc;

import android.app.*;
import android.content.pm.*;
import android.os.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * uses-permission android:name="android.permission.FORCE_STOP_PACKAGES"
 * android:theme="@android:style/Theme.NoDisplay"
 */
public class KillActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(new Runnable() {
            public void run() {
                try {
                    List<String> packageNames = new LinkedList<>();
                    List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
                    for (PackageInfo pi : installedPackages)
                        if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 &&
                                (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) packageNames.add(pi.packageName);
                    Object am = getSystemService(ACTIVITY_SERVICE);
                    Method m = am.getClass().getMethod("forceStopPackage", String.class);
                    for (String packageName : packageNames) m.invoke(am, packageName);
                } catch (Exception e) {e.printStackTrace();}
            }
        }).start();
        finish();
    }
}
