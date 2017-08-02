package com.xdandroid.sample.misc;

import android.app.*;
import android.content.pm.*;
import android.os.*;
import android.text.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * uses-permission android:name="android.permission.UPDATE_APP_OPS_STATS"
 * android:theme="@android:style/Theme.NoDisplay"
 */
public class RevokeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        new Thread(() -> {
            try {
                PackageManager pm = getPackageManager();
                Class<AppOpsManager> aomClass = AppOpsManager.class;
                AppOpsManager aom = getSystemService(aomClass);
                Method setUidModeMethod = aomClass.getMethod("setUidMode", String.class, int.class, int.class);
                Method setModeMethod = aomClass.getMethod("setMode", int.class, int.class, String.class, int.class);
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                  .parallelStream()
                  .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                  .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
                  .forEach(i -> {
                      int uid = i.applicationInfo.uid;
                      String n = i.applicationInfo.packageName;
                      try { setModeMethod.invoke(aom, 10, uid, n, AppOpsManager.MODE_IGNORED); } catch (Exception e) { e.printStackTrace(); }
                      try { setModeMethod.invoke(aom, 40, uid, n, AppOpsManager.MODE_IGNORED); } catch (Exception e) { e.printStackTrace(); }
                      try { setModeMethod.invoke(aom, 63, uid, n, AppOpsManager.MODE_IGNORED); } catch (Exception e) { e.printStackTrace(); }
                      if (i.applicationInfo.targetSdkVersion < Build.VERSION_CODES.M && i.requestedPermissions != null) {
                          Arrays.stream(i.requestedPermissions)
                                .parallel()
                                .map(p -> {
                                    try { return pm.getPermissionInfo(p, 0); } catch (Exception e) { return null; }
                                })
                                .filter(Objects::nonNull)
                                .filter(pi -> pi.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS
                                        || pi.protectionLevel == 4096 + PermissionInfo.PROTECTION_DANGEROUS)
                                .map(pi -> pi.name)
                                .filter(pn -> pn.startsWith("android"))
                                .filter(pn -> !"android.permission.READ_EXTERNAL_STORAGE".equals(pn))
                                .filter(pn -> !"android.permission.WRITE_EXTERNAL_STORAGE".equals(pn))
                                .map(AppOpsManager::permissionToOp)
                                .filter(op -> !TextUtils.isEmpty(op))
                                .forEach(op -> {
                                    try { setUidModeMethod.invoke(aom, op, uid, AppOpsManager.MODE_IGNORED); } catch (Exception e) { e.printStackTrace(); }
                                });
                          try { setModeMethod.invoke(aom, 23, uid, n, AppOpsManager.MODE_IGNORED); } catch (Exception e) { e.printStackTrace(); }
                          try { setModeMethod.invoke(aom, 24, uid, n, AppOpsManager.MODE_IGNORED); } catch (Exception e) { e.printStackTrace(); }
                      }
                  });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
        finish();
    }
}
