package com.xdandroid.hellodaemon.misc;

import android.annotation.*;
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

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            AppOpsManager appOps = getSystemService(AppOpsManager.class);
            try {
                Method setUidModeMethod;
                boolean marshmallow = false;
                Method permissionToOpCodeMethod = null;
                try {
                    setUidModeMethod = AppOpsManager.class.getMethod("setUidMode", String.class, int.class, int.class);
                } catch (NoSuchMethodException e) {
                    marshmallow = true;
                    setUidModeMethod = AppOpsManager.class.getMethod("setUidMode", int.class, int.class, int.class);
                    permissionToOpCodeMethod = AppOpsManager.class.getMethod("permissionToOpCode", String.class);
                }
                Method setModeMethod = AppOpsManager.class.getMethod("setMode", int.class, int.class, String.class, int.class);
                for (PackageInfo packageInfo : installedPackages) {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ||
                            (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) continue;
                    if (packageInfo.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.M) continue;
                    int uid = packageInfo.applicationInfo.uid;
                    if (packageInfo.requestedPermissions == null || packageInfo.requestedPermissions.length <= 0) continue;
                    for (String requestedPermission : packageInfo.requestedPermissions) {
                        PermissionInfo permissionInfo = null;
                        try {
                            permissionInfo = pm.getPermissionInfo(requestedPermission, 0);
                        } catch (PackageManager.NameNotFoundException ignored) {}
                        if (permissionInfo == null || permissionInfo.protectionLevel != PermissionInfo.PROTECTION_DANGEROUS) continue;
                        if (!permissionInfo.name.startsWith("android")) continue;
                        if (!marshmallow) {
                            String appOp = AppOpsManager.permissionToOp(permissionInfo.name);
                            if (!TextUtils.isEmpty(appOp)) setUidModeMethod.invoke(appOps, appOp, uid, AppOpsManager.MODE_IGNORED);
                        } else {
                            try {
                                int code = (int) permissionToOpCodeMethod.invoke(appOps, permissionInfo.name);
                                setUidModeMethod.invoke(appOps, code, uid, AppOpsManager.MODE_IGNORED);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    setModeMethod.invoke(appOps, 24, uid, packageInfo.applicationInfo.packageName, AppOpsManager.MODE_IGNORED);
                    setModeMethod.invoke(appOps, 23, uid, packageInfo.applicationInfo.packageName, AppOpsManager.MODE_IGNORED);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        finish();
    }
}
