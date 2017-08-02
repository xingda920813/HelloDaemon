package com.xdandroid.sample.misc;

import android.*;
import android.app.*;
import android.content.pm.*;
import android.os.*;
import android.text.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
 * android:theme="@android:style/Theme.NoDisplay"
 */
public class GenOpsActivity extends Activity {

    static String genOp(String pkg, String op) {
        return "adb shell cmd appops set " + pkg + " " + op + " ignore\n\n";
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable, R extends RuntimeException> R asUnchecked(Throwable t) throws E {
        throw (E) t;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, hashCode());
            finish();
            return;
        }
        new Thread(() -> {
            try (FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "ops.sh"))) {
                PackageManager pm = getPackageManager();
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
                  .parallelStream()
                  .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                  .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
                  .map(i -> {
                      String n = i.applicationInfo.packageName;
                      Set<String> ops = new HashSet<>();
                      ops.add(genOp(n, "WIFI_SCAN"));
                      ops.add(genOp(n, "WAKE_LOCK"));
                      ops.add(genOp(n, "RUN_IN_BACKGROUND"));
                      if (i.applicationInfo.targetSdkVersion < Build.VERSION_CODES.M && i.requestedPermissions != null) {
                          ops.addAll(Arrays
                                  .stream(i.requestedPermissions)
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
                                  .map(op -> genOp(n, op))
                                  .collect(Collectors.toSet()));
                          ops.add(genOp(n, "WRITE_SETTINGS"));
                          ops.add(genOp(n, "SYSTEM_ALERT_WINDOW"));
                      }
                      return ops;
                  })
                  .flatMap(Collection::parallelStream)
                  .forEach(op -> {
                      try {
                          fos.write(op.getBytes("UTF-8"));
                      } catch (IOException e) {
                          throw asUnchecked(e);
                      }
                  });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
        finish();
    }
}
