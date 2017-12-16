package com.xdandroid.sample.misc.server;

import android.app.*;

/**
 * coreApp="true"
 * android:sharedUserId="android.uid.system"
 * android:process="system"
 * android:theme="@android:style/Theme.NoDisplay"
 */
public class TargetActivity extends Activity {

/*    static final List<String> WHITE_LIST_APPS = Arrays.asList(
            "com.breel.wallpapers",
            "com.github.shadowsocks",
            "com.xdandroid.kill",
            "com.xdandroid.server",
            "me.piebridge.brevent",

            "com.alibaba.android.rimet",
            "com.bearyinnovative.horcrux",
            "com.tencent.mm",
            "com.tencent.tim",
            "com.alibaba.alimei"
    );

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        new Thread(() -> {
            try {
                IPackageManager pm = (IPackageManager) ServiceManager.getService("package");
                Field packagesField = pm.getClass().getDeclaredField("mPackages");
                packagesField.setAccessible(true);
                final Field[] appInfoField = {null};
                ((ArrayMap<String, ?>) packagesField.get(pm))
                        .values()
                        .stream()
                        .map(pkg -> {
                            try {
                                if (appInfoField[0] == null) {
                                    appInfoField[0] = pkg.getClass().getDeclaredField("applicationInfo");
                                    appInfoField[0].setAccessible(true);
                                }
                                return (ApplicationInfo) appInfoField[0].get(pkg);
                            } catch (Exception e) { return null; }
                        })
                        .filter(Objects::nonNull)
                        .filter(appInfo -> !WHITE_LIST_APPS.contains(appInfo.packageName))
                        .forEach(appInfo -> {
                            if (appInfo.targetSdkVersion >= Build.VERSION_CODES.M) appInfo.targetSdkVersion = Build.VERSION.SDK_INT;
                            if (appInfo.targetSdkVersion <= Build.VERSION_CODES.LOLLIPOP_MR1) appInfo.targetSdkVersion = Build.VERSION_CODES.LOLLIPOP_MR1;
                        });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
        finish();
    }*/
}
