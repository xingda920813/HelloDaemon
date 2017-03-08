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
        new Thread(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
                PackageManager pm = getPackageManager();
                List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
                Class<AppOpsManager> aomClazz = AppOpsManager.class;
                AppOpsManager aom = getSystemService(aomClazz);
                boolean nougat = true;
                try {
                    Method setUidModeMethod;
                    Method permissionToOpCodeMethod = null;
                    Method setModeMethod = aomClazz.getMethod("setMode", int.class, int.class, String.class, int.class);
                    try {
                        setUidModeMethod = aomClazz.getMethod("setUidMode", String.class, int.class, int.class);
                    } catch (Exception e) {
                        nougat = false;
                        setUidModeMethod = aomClazz.getMethod("setUidMode", int.class, int.class, int.class);
                        permissionToOpCodeMethod = aomClazz.getMethod("permissionToOpCode", String.class);
                    }
                    for (PackageInfo pi : installedPackages) {
                        if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ||
                                (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) continue;
                        if (pi.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.M) continue;
                        if (pi.requestedPermissions == null || pi.requestedPermissions.length <= 0) continue;
                        int uid = pi.applicationInfo.uid;
                        for (String reqPermission : pi.requestedPermissions) {
                            PermissionInfo permissionInfo = null;
                            try {permissionInfo = pm.getPermissionInfo(reqPermission, 0);} catch (Exception ignored) {}
                            if (permissionInfo == null || permissionInfo.protectionLevel != PermissionInfo.PROTECTION_DANGEROUS) continue;
                            if (!permissionInfo.name.startsWith("android")) continue;
                            if (nougat) {
                                String op = AppOpsManager.permissionToOp(permissionInfo.name);
                                if (!TextUtils.isEmpty(op)) setUidModeMethod.invoke(aom, op, uid, AppOpsManager.MODE_IGNORED);
                            } else {
                                try {
                                    int code = (int) permissionToOpCodeMethod.invoke(aom, permissionInfo.name);
                                    setUidModeMethod.invoke(aom, code, uid, AppOpsManager.MODE_IGNORED);
                                } catch (Exception e) {e.printStackTrace();}
                            }
                        }
                        setModeMethod.invoke(aom, 24, uid, pi.applicationInfo.packageName, AppOpsManager.MODE_IGNORED);
                        setModeMethod.invoke(aom, 23, uid, pi.applicationInfo.packageName, AppOpsManager.MODE_IGNORED);
                    }
                } catch (Exception e) {e.printStackTrace();}
            }
        }).start();
        finish();
    }
}
