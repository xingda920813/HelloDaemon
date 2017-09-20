package com.xdandroid.sample.misc;

import android.*;
import android.app.*;
import android.content.pm.*;
import android.os.*;

import java.io.*;
import java.util.stream.*;

import static com.xdandroid.sample.misc.RevokeActivity.*;

/**
 * uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
 * android:theme="@android:style/Theme.NoDisplay"
 */
public class GenOpsActivity extends Activity {

    static String genOp(String pkg, String op) {
        String mode;
        switch (op) {
            case "RUN_IN_BACKGROUND":
                mode = WHITE_LIST_APPS.contains(pkg) ? "allow" : "ignore";
                break;
            default:
                mode = "ignore";
                break;
        }
        return "adb shell cmd appops set " + pkg + " " + op + " " + mode + "\n\n";
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
                getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS)
                                   .stream()
                                   .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                                   .filter(i -> (i.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
                                   .map(i -> i.applicationInfo.packageName)
                                   .flatMap(n -> Stream.of(BLACK_LIST_OPS).map(op -> genOp(n, op)))
                                   .forEach(op -> {
                                       try { fos.write(op.getBytes("UTF-8")); } catch (IOException e) { throw asUnchecked(e); }
                                   });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
        finish();
    }
}
