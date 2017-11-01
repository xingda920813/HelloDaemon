package com.xdandroid.sample.misc;

import android.annotation.*;
import android.os.*;

import java.util.*;

interface Utils {

    List<String> WHITE_LIST_APPS = Arrays.asList(
            "com.breel.wallpapers",
            "com.github.shadowsocks",
            "com.xdandroid.kill",
            "me.piebridge.brevent",

            "com.alibaba.android.rimet",
            "com.bearyinnovative.horcrux",
            "com.tencent.mm",
            "com.tencent.mobileqq",

            "com.alibaba.alimei",
            "com.tencent.androidqqmail"
    );

    List<String> WHITE_LIST_PERMISSIONS = Arrays.asList(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    );

    String[] BLACK_LIST_OPS = {
            "WIFI_SCAN",
            "WAKE_LOCK",
            "RUN_IN_BACKGROUND",
            "OP_BOOT_COMPLETED",
            "WRITE_SETTINGS",
            "SYSTEM_ALERT_WINDOW"
    };

    List<String> WHITE_LIST_OPS_FOR_WHITE_LIST_APPS = Arrays.asList(
            "RUN_IN_BACKGROUND",
            "OP_BOOT_COMPLETED"
    );

    int CM_SDK_INT = getInt("ro.cm.build.version.plat.sdk", 0);

    @SuppressLint("PrivateApi")
    static int getInt(String key, int def) {
        try {
            return (int) Class.forName("android.os.SystemProperties").getMethod("getInt", String.class, int.class).invoke(null, key, def);
        } catch (Exception e) {
            e.printStackTrace();
            return def;
        }
    }

    default boolean shouldDisableBootCompletedOp() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 && CM_SDK_INT >= 6;
    }

    @SuppressWarnings("unchecked")
    default <E extends Throwable, R extends RuntimeException> R asUnchecked(Throwable t) throws E {
        throw (E) t;
    }

    default String genOp(String pkg, String op) {
        return "adb shell cmd appops set " + pkg + " " + op + " " + (WHITE_LIST_OPS_FOR_WHITE_LIST_APPS.contains(op) && WHITE_LIST_APPS.contains(pkg) ? "allow" : "ignore") + "\n\n";
    }
}
