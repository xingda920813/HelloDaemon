package com.xdandroid.hellodaemon.util;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.provider.*;

import com.xdandroid.hellodaemon.*;

import java.util.*;

/**
 * Created by xingda on 16-9-13.
 */

public class IntentWrapper {

    //Android 6.0+ Doze 模式
    public static final int DOZE = 98;
    //华为 自启动管理
    public static final int HUAWEI = 99;
    //华为 受保护的应用
    public static final int HUAWEI_GOD = 100;
    //小米 自启动管理
    public static final int XIAOMI = 101;
    //小米 神隐模式 (建议只在 App 的核心功能需要后台连接网络/后台定位的情况下使用)
    public static final int XIAOMI_GOD = 102;
    //三星 5.0+ 智能管理器
    public static final int SAMSUNG = 103;
    //魅族 自启动管理
    public static final int MEIZU = 104;
    //魅族 待机耗电管理
    public static final int MEIZU_GOD = 105;
    //Oppo 自启动管理
    public static final int OPPO = 106;
    //Oppo 纯净后台应用管控
    public static final int OPPO_GOD = 107;
    //Vivo 后台高耗电
    public static final int VIVO_GOD = 109;

    public static final List<IntentWrapper> sIntentWrapperList;

    private static final Application sApp;

    static {

        sApp = MainActivity.sApp;

        sIntentWrapperList = new ArrayList<>();

        //Android 6.0+ Doze 模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) sApp.getSystemService(Context.POWER_SERVICE);
            boolean ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(sApp.getPackageName());
            if (!ignoringBatteryOptimizations) {
                Intent dozeIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                dozeIntent.setData(Uri.parse("package:" + sApp.getPackageName()));
                sIntentWrapperList.add(new IntentWrapper(dozeIntent, IntentWrapper.DOZE));
            }
        }

        //华为 自启动管理
        Intent huaweiIntent = new Intent();
        huaweiIntent.setAction("huawei.intent.action.HSM_BOOTAPP_MANAGER");
        sIntentWrapperList.add(new IntentWrapper(huaweiIntent, IntentWrapper.HUAWEI));

        //华为 受保护的应用
        Intent huaweiGodIntent = new Intent();
        huaweiGodIntent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        sIntentWrapperList.add(new IntentWrapper(huaweiGodIntent, IntentWrapper.HUAWEI_GOD));

        //小米 自启动管理
        Intent xiaomiIntent = new Intent();
        xiaomiIntent.setAction("miui.intent.action.OP_AUTO_START");
        xiaomiIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sIntentWrapperList.add(new IntentWrapper(xiaomiIntent, IntentWrapper.XIAOMI));

        //小米 神隐模式 (建议只在 App 的核心功能需要后台连接网络/后台定位的情况下使用)
        Intent xiaomiGodIntent = new Intent();
        xiaomiGodIntent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"));
        sIntentWrapperList.add(new IntentWrapper(xiaomiGodIntent, IntentWrapper.XIAOMI_GOD));

        //三星 5.0+ 智能管理器
        Intent samsungIntent = MainActivity.sApp.getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm");
        if (samsungIntent != null) sIntentWrapperList.add(new IntentWrapper(samsungIntent, IntentWrapper.SAMSUNG));

        //魅族 自启动管理
        Intent meizuIntent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        meizuIntent.addCategory(Intent.CATEGORY_DEFAULT);
        meizuIntent.putExtra("packageName", MainActivity.sApp.getPackageName());
        sIntentWrapperList.add(new IntentWrapper(meizuIntent, IntentWrapper.MEIZU));

        //魅族 待机耗电管理
        Intent meizuGodIntent = new Intent();
        meizuGodIntent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.powerui.AppPowerManagerActivity"));
        sIntentWrapperList.add(new IntentWrapper(meizuGodIntent, IntentWrapper.MEIZU_GOD));

        //Oppo 自启动管理
        Intent oppoIntent = new Intent();
        oppoIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.startup.StartupAppListActivity"));
        sIntentWrapperList.add(new IntentWrapper(oppoIntent, IntentWrapper.OPPO));

        //Oppo 纯净后台应用管控
        Intent oppoGodIntent = new Intent();
        oppoGodIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.purebackground.PureBackgroundSettingActivity"));
        sIntentWrapperList.add(new IntentWrapper(oppoGodIntent, IntentWrapper.OPPO_GOD));

        //Vivo 后台高耗电
        Intent vivoGodIntent = new Intent();
        vivoGodIntent.setComponent(new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"));
        sIntentWrapperList.add(new IntentWrapper(vivoGodIntent, IntentWrapper.VIVO_GOD));
    }

    private IntentWrapper(Intent intent, int type) {
        this.intent = intent;
        this.type = type;
    }

    private Intent intent;
    //以上常量
    public int type;

    /**
     * 安全地启动一个Activity
     */
    public void startActivity(Context context) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断本机上是否有能处理当前Intent的Activity
     */
    public boolean doesActivityExists(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }
}
