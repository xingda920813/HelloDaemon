package com.xdandroid.sample.misc.server;

import android.app.*;

/**
 * coreApp="true"
 * android:sharedUserId="android.uid.system"
 * android:process="system"
 * android:theme="@android:style/Theme.NoDisplay"
 */
public class TargetActivity extends Activity {

/*    @SuppressWarnings("unchecked")
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
                        .forEach(appInfo -> {
                            if (appInfo.targetSdkVersion >= Build.VERSION_CODES.M) appInfo.targetSdkVersion = Build.VERSION.SDK_INT;
                            if (appInfo.targetSdkVersion <= Build.VERSION_CODES.LOLLIPOP_MR1) appInfo.targetSdkVersion = Build.VERSION_CODES.LOLLIPOP_MR1;
                        });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
        finish();
    }*/
}
