package com.xdandroid.hellodaemon;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.xdandroid.hellodaemon.service.*;
import com.xdandroid.hellodaemon.util.*;

public class MainActivity extends Activity {

    public static Application sApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sApp = getApplication();
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(v -> startService(new Intent(this, WorkService.class)));
        findViewById(R.id.btn_jump).setOnClickListener(this::whiteListMatters);
    }

    /**
     * 处理白名单
     */
    private void whiteListMatters(View v) {
        boolean nothingMatches = true;
        for (IntentWrapper intentWrapper : IntentWrapper.sIntentWrapperList) {
            //如果本机上没有能处理这个Intent的Activity，说明不是对应的机型，直接忽略进入下一次循环。
            if (!intentWrapper.doesActivityExists(this)) continue;
            switch (intentWrapper.type) {
                case IntentWrapper.DOZE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        if (pm.isIgnoringBatteryOptimizations(getPackageName())) break;
                        nothingMatches = false;
                        new AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle("需要忽略 HelloDaemon 的的电池优化")
                                .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 加入到电池优化的忽略名单。\n\n" +
                                        "请点击『确定』，在弹出的『忽略电池优化』对话框中，选择『是』。")
                                .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                                .show();
                    }
                    break;
                case IntentWrapper.HUAWEI:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 HelloDaemon 自动启动")
                            .setMessage("轨迹跟踪服务的后台运行需要允许 HelloDaemon 的后台自动启动。\n\n" +
                                    "请点击『确定』，在弹出的自动启动管理页面中，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.HUAWEI_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("HelloDaemon 需要加入受保护的应用名单")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 加入到受保护的应用名单。\n\n" +
                                    "请点击『确定』，在弹出的受保护应用列表中，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.XIAOMI:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("HelloDaemon 需要加入自启动白名单")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 加入到自启动白名单。\n\n" +
                                    "请点击『确定』，在弹出的自启动管理界面中，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.XIAOMI_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要关闭 HelloDaemon 的神隐模式")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 的神隐模式关闭。\n\n" +
                                    "请点击『确定』，在弹出的神隐模式应用列表中，点击 HelloDaemon ，然后选择『无限制』和『允许定位』。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.SAMSUNG:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 HelloDaemon 的自启动")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 在屏幕关闭时继续运行。\n\n" +
                                    "请点击『确定』，在弹出的 智能管理器 中，点击『内存』，选择『自启动应用程序』选项卡，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.MEIZU:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("需要允许 HelloDaemon 的自启动")
                            .setMessage("轨迹跟踪服务的后台运行需要允许 HelloDaemon 的自启动。\n\n" +
                                    "请点击『确定』，在弹出的应用信息界面中，将『自启动』开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
                case IntentWrapper.MEIZU_GOD:
                    nothingMatches = false;
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("HelloDaemon 需要在待机时保持运行")
                            .setMessage("轨迹跟踪服务的后台运行需要 HelloDaemon 在待机时保持运行。\n\n" +
                                    "请点击『确定』，在弹出的『待机耗电管理』中，将 HelloDaemon 对应的开关打开。")
                            .setPositiveButton("确定", (dialog, which) -> intentWrapper.startActivity(this))
                            .show();
                    break;
            }
        }
        if (nothingMatches) Toast.makeText(this, "不是对应的机型", Toast.LENGTH_SHORT).show();
    }

    /**
     * 防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
     */
    @Override
    public void onBackPressed() {
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(launcherIntent);
    }
}
