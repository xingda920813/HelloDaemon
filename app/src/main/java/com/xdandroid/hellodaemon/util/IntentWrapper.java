package com.xdandroid.hellodaemon.util;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.text.*;

import java.util.*;

import static com.xdandroid.hellodaemon.MainActivity.sApp;

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
    //Vivo 自启动管理
    public static final int VIVO = 108;
    //Vivo 后台高耗电
    public static final int VIVO_GOD = 109;
    //金立 应用自启
    public static final int GIONEE = 110;
    //乐视 自启动管理
    public static final int LETV = 111;
    //乐视 应用保护
    public static final int LETV_GOD = 112;
    //酷派 自启动管理
    public static final int COOLPAD = 113;
    //联想 后台管理
    public static final int LENOVO = 114;
    //联想 后台耗电优化
    public static final int LENOVO_GOD = 115;
    //中兴 自启管理
    public static final int ZTE = 116;
    //中兴 锁屏加速受保护应用
    public static final int ZTE_GOD = 117;

    public static final List<IntentWrapper> INTENT_WRAPPER_LIST;

    static {

        INTENT_WRAPPER_LIST = new ArrayList<>();

        //Android 6.0+ Doze 模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PowerManager pm = (PowerManager) sApp.getSystemService(Context.POWER_SERVICE);
            boolean ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(sApp.getPackageName());
            if (!ignoringBatteryOptimizations) {
                Intent dozeIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                dozeIntent.setData(Uri.parse("package:" + sApp.getPackageName()));
                INTENT_WRAPPER_LIST.add(new IntentWrapper(dozeIntent, DOZE));
            }
        }

        //华为 自启动管理
        Intent huaweiIntent = new Intent();
        huaweiIntent.setAction("huawei.intent.action.HSM_BOOTAPP_MANAGER");
        INTENT_WRAPPER_LIST.add(new IntentWrapper(huaweiIntent, HUAWEI));

        //华为 受保护的应用
        Intent huaweiGodIntent = new Intent();
        huaweiGodIntent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(huaweiGodIntent, HUAWEI_GOD));

        //小米 自启动管理
        Intent xiaomiIntent = new Intent();
        xiaomiIntent.setAction("miui.intent.action.OP_AUTO_START");
        xiaomiIntent.addCategory(Intent.CATEGORY_DEFAULT);
        INTENT_WRAPPER_LIST.add(new IntentWrapper(xiaomiIntent, XIAOMI));

        //小米 神隐模式 (建议只在 App 的核心功能需要后台连接网络/后台定位的情况下使用)
        Intent xiaomiGodIntent = new Intent();
        xiaomiGodIntent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(xiaomiGodIntent, XIAOMI_GOD));

        //三星 5.0+ 智能管理器
        Intent samsungIntent = sApp.getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm");
        if (samsungIntent != null) INTENT_WRAPPER_LIST.add(new IntentWrapper(samsungIntent, SAMSUNG));

        //魅族 自启动管理
        Intent meizuIntent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        meizuIntent.addCategory(Intent.CATEGORY_DEFAULT);
        meizuIntent.putExtra("packageName", sApp.getPackageName());
        INTENT_WRAPPER_LIST.add(new IntentWrapper(meizuIntent, MEIZU));

        //魅族 待机耗电管理
        Intent meizuGodIntent = new Intent();
        meizuGodIntent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.powerui.AppPowerManagerActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(meizuGodIntent, MEIZU_GOD));

        //Oppo 自启动管理
        Intent oppoIntent = new Intent();
        oppoIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.startup.StartupAppListActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(oppoIntent, OPPO));

        //Oppo 纯净后台应用管控
        Intent oppoGodIntent = new Intent();
        oppoGodIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.purebackground.PureBackgroundSettingActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(oppoGodIntent, OPPO_GOD));

        //Vivo 自启动管理
        Intent vivoIntent = new Intent();
        vivoIntent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.MainActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(vivoIntent, VIVO));

        //Vivo 后台高耗电
        Intent vivoGodIntent = new Intent();
        vivoGodIntent.setComponent(new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(vivoGodIntent, VIVO_GOD));

        //金立 应用自启
        Intent gioneeIntent = new Intent();
        gioneeIntent.setComponent(new ComponentName("com.gionee.softmanager", "com.gionee.softmanager.MainActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(gioneeIntent, GIONEE));

        //乐视 自启动管理
        Intent letvIntent = new Intent();
        letvIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(letvIntent, LETV));

        //乐视 应用保护
        Intent letvGodIntent = new Intent();
        letvGodIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.BackgroundAppManageActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(letvGodIntent, LETV_GOD));

        //酷派 自启动管理
        Intent coolpadIntent = new Intent();
        coolpadIntent.setComponent(new ComponentName("com.yulong.android.security", "com.yulong.android.seccenter.tabbarmain"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(coolpadIntent, COOLPAD));

        //联想 后台管理
        Intent lenovoIntent = new Intent();
        lenovoIntent.setComponent(new ComponentName("com.lenovo.security", "com.lenovo.security.purebackground.PureBackgroundActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(lenovoIntent, LENOVO));

        //联想 后台耗电优化
        Intent lenovoGodIntent = new Intent();
        lenovoGodIntent.setComponent(new ComponentName("com.lenovo.powersetting", "com.lenovo.powersetting.ui.Settings$HighPowerApplicationsActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(lenovoGodIntent, LENOVO_GOD));

        //中兴 自启管理
        Intent zteIntent = new Intent();
        zteIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(zteIntent, ZTE));

        //中兴 锁屏加速受保护应用
        Intent zteGodIntent = new Intent();
        zteGodIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.setting.ClearAppSettingsActivity"));
        INTENT_WRAPPER_LIST.add(new IntentWrapper(zteGodIntent, ZTE_GOD));
    }

    IntentWrapper(Intent intent, int type) {
        mIntent = intent;
        mType = type;
    }

    Intent mIntent;
    public int mType;

    /**
     * 安全地启动一个Activity
     */
    public void startActivity(Activity activity) {
        try {activity.startActivity(mIntent);} catch (Exception e) {e.printStackTrace();}
    }

    /**
     * 判断本机上是否有能处理当前Intent的Activity
     */
    public boolean doesActivityExists() {
        PackageManager pm = sApp.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return list != null && list.size() > 0;
    }

    static String sApplicationName;

    public static String getApplicationName() {
        if (!TextUtils.isEmpty(sApplicationName)) return sApplicationName;
        PackageManager packageManager;
        ApplicationInfo applicationInfo;
        try {
            packageManager = sApp.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(sApp.getPackageName(), 0);
            sApplicationName = packageManager.getApplicationLabel(applicationInfo).toString();
            return sApplicationName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return sApp.getPackageName();
        }
    }
}
