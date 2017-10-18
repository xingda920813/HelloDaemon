package com.xdandroid.sample.misc;

import android.app.*;
import android.content.pm.*;
import android.os.*;

import java.lang.reflect.*;

/**
 * uses-permission android:name="android.permission.FORCE_STOP_PACKAGES"
 * android:theme="@android:style/Theme.NoDisplay"
 */
public class KillActivity extends Activity implements Utils {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        new Thread(() -> {
            try {
                Method m = ActivityManager.class.getMethod("forceStopPackage", String.class);
                ActivityManager am = getSystemService(ActivityManager.class);
                getPackageManager().getInstalledPackages(0)
                                   .stream()
                                   .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                                   .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
                                   .map(i -> i.packageName)
                                   .filter(n -> !WHITE_LIST_APPS.contains(n))
                                   .forEach(n -> {
                                       try { m.invoke(am, n); } catch (Exception e) { e.printStackTrace(); }
                                   });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
        finish();
    }
}
